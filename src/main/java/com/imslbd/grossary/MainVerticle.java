package com.imslbd.grossary;

import com.imslbd.grossary.controller.*;
import com.imslbd.grossary.service.*;
import com.imslbd.grossary.template.page.MyLoginTemplate;
import io.crm.QC;
import io.crm.promise.Promises;
import io.crm.web.ApiEvents;
import io.crm.web.Uris;
import io.crm.web.codec.ListToListCodec;
import io.crm.web.codec.RspListToRspListCodec;
import io.crm.web.controller.AuthController;
import io.crm.web.controller.GoogleMapController;
import io.crm.web.statichandler.StaticHandler;
import io.crm.web.template.PageBuilder;
import io.crm.web.util.RspList;
import io.crm.web.util.WebUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;

final public class MainVerticle extends AbstractVerticle {
    private final Set<String> publicUris = publicUris();
    public static final String PORT_PROPERTY = "PORT";
    public static final int PORT = MyApp.loadConfig().getInteger(PORT_PROPERTY);
    private JDBCClient jdbcClient;

    @Override
    public void start() throws Exception {
        initialize();
        registerCodecs();
        registerEvents();

        //Configure Router
        final Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());
        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);
        router.route().handler(sessionHandler);

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx)
            .bridge(bridgeOptions()));

        //Register Listeners
        registerFilters(router);

        registerStaticFileHandlers(router);

        otherwiseController(router);

        registerControllers(router);

        HttpServer server = getVertx().createHttpServer().requestHandler(router::accept).listen(PORT);

        System.out.println("<----------------------------------WEB_SERVER_STARTED------------------------------------->");
        System.out.println("PORT: " + PORT);
    }

    private BridgeOptions bridgeOptions() {
        return new BridgeOptions()
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.REGION_WISE_CALL_SUMMARY))
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.AREA_WISE_CALL_SUMMARY))
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.DISTRIBUTION_HOUSE_WISE_CALL_SUMMARY))
            .addInboundPermitted(new PermittedOptions().setAddress(MyEvents.BR_WISE_CALL_SUMMARY));
    }

    private PermittedOptions outboundPermitteds() {
        return new PermittedOptions().setAddressRegex(".+");
    }

    private PermittedOptions inboundPermitteds() {
        return new PermittedOptions().setAddressRegex(".+");
    }

    private void initialize() {

        jdbcClient = JDBCClient.createShared(vertx, MyApp.dbConfig());
    }

    private void registerCodecs() {
        vertx.eventBus().registerDefaultCodec(RspList.class, new RspListToRspListCodec());
        vertx.eventBus().registerDefaultCodec(List.class, new ListToListCodec());
    }

    @Override
    public void stop() throws Exception {
        Promises.from(jdbcClient).then(jdbcClient -> {
            if (jdbcClient != null) jdbcClient.close();
        });
    }

    private void registerEvents() {

        final EventBus eventBus = vertx.eventBus();
        AuthService authService = new AuthService(vertx, jdbcClient);
        eventBus.consumer(ApiEvents.LOGIN_API, authService::login);

        FileUploaderService fileUploaderService = new FileUploaderService(vertx);
        vertx.eventBus().consumer(MyEvents.SAVE_BYTEARRAY, fileUploaderService::saveByteArray);

        CRUDService crudService = new CRUDService(vertx, jdbcClient);
        vertx.eventBus().consumer(MyEvents.INSERT, crudService::insert);
        vertx.eventBus().consumer(MyEvents.UPDATE, crudService::update);
        vertx.eventBus().consumer(MyEvents.DELETE, crudService::delete);
        vertx.eventBus().consumer(MyEvents.QUERY, crudService::query);

        ContactService contactService = new ContactService(vertx, jdbcClient);
        vertx.eventBus().consumer(MyEvents.FIND_ALL_CONTACTS_GROCERIES, contactService::findAllContactsGroceries);
        vertx.eventBus().consumer(MyEvents.FIND_ALL_CONTACTS_LOCATIONS, contactService::findAllContactsLocations);
        vertx.eventBus().consumer(MyEvents.FIND_ALL_CONTACTS_POS_NOS, contactService::findAllContactsPosNos);
        vertx.eventBus().consumer(MyEvents.FIND_CONTACTS_DATE_MIN_MAX, contactService::findContactsDateMinMax);
        vertx.eventBus().consumer(MyEvents.FIND_ALL_CONTACTS, contactService::findContacts);
        vertx.eventBus().consumer(MyEvents.CONTACTS_COUNT_BY_DATE, contactService::countByDate);
        vertx.eventBus().consumer(MyEvents.CONTACTS_SUMMARY, contactService::contactsSummary);
        vertx.eventBus().consumer(MyEvents.GROUP_BY_COUNT_CONTACTS, contactService::groupByCount);
        vertx.eventBus().consumer(MyEvents.CONTACTS_SUMMARY_DETAILS, contactService::summaryDetails);


        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions()
            .setDefaultHost(MyApp.loadConfig().getString("CALL_REVIEW_HOST"))
            .setDefaultPort(MyApp.loadConfig().getInteger("CALL_REVIEW_PORT")));
        CallCenterService callCenterService = new CallCenterService(httpClient);
        vertx.eventBus().consumer(MyEvents.REGION_WISE_CALL_SUMMARY, callCenterService::regionWiseCallSummary);
        vertx.eventBus().consumer(MyEvents.AREA_WISE_CALL_SUMMARY, callCenterService::areaWiseCallSummary);
        vertx.eventBus().consumer(MyEvents.DISTRIBUTION_HOUSE_WISE_CALL_SUMMARY, callCenterService::distributionHouseWiseCallSummary);
        vertx.eventBus().consumer(MyEvents.BR_WISE_CALL_SUMMARY, callCenterService::brWiseCallSummary);



    }

    private void registerFilters(final Router router) {
        corsFilter(router);
        noCacheFilter(router);
        authFilter(router);
    }

    private void corsFilter(final Router router) {
        router.route().handler(context -> {
            context.response().headers()
                .set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, DELETE")
                .set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600")
                .set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Authorization, X-Requested-With")
            ;
            context.next();
        });
    }

    private void noCacheFilter(final Router router) {
        router.route().handler(context -> {
            if (!context.request().uri().startsWith("/static")) {
                context.response().headers().set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                context.response().headers().set("Pragma", "no-cache");
                context.response().headers().set(HttpHeaders.EXPIRES, "0");
            }
            context.next();
        });
    }

    private void authFilter(final Router router) {

        router.route().handler(ctx -> {
            if (ctx.request().method() == HttpMethod.OPTIONS) {
                ctx.response().end();
                return;
            }
            if (System.getProperty("dev-mode") != null) {
                ctx.session().put(ss.currentUser,
                    new JsonObject()
                        .put(QC.username, "Sohan")
                        .put(QC.userId, "br-124")
                        .put(QC.mobile, "01553661069")
                        .put(QC.userType,
                            new JsonObject()
                                .put(QC.id, 1)
                                .put(QC.name, "Programmer")));
            }
            if (ctx.session().get(ss.currentUser) != null) {
                ctx.next();
                return;
            } else if (GroceryController.authToken.equals(ctx.request().getHeader(HttpHeaders.AUTHORIZATION.toString()))) {
                ctx.session().put(ss.currentUser,
                    new JsonObject()
                        .put(QC.username, "anonymous")
                        .put(QC.userId, "anonym-16424562")
                        .put(QC.mobile, "")
                        .put(QC.userType,
                            new JsonObject()
                                .put(QC.id, 15)
                                .put(QC.name, "")));
                ctx.next();
                return;
            }
            final String uri = ctx.request().uri();

            if (publicUris.stream().filter(publicUri -> uri.startsWith(publicUri)).findAny().isPresent()) {
                ctx.next();
                return;
            }
            //Auth Failed
            if ("XMLHttpRequest".equalsIgnoreCase(ctx.request().headers().get("X-Requested-With"))) {
                ctx.response().setStatusCode(HttpResponseStatus.FORBIDDEN.code())
                    .end("Please login to authorize your request.");
                return;
            }
            ctx.response().setStatusCode(HttpResponseStatus.TEMPORARY_REDIRECT.code());
            ctx.response().headers().set(HttpHeaders.LOCATION, Uris.LOGIN.value);
            ctx.response().end();
        });
    }

    private void registerControllers(final Router router) {

        loginFormController(router);
        AuthController authCtrl = new AuthController(vertx, router);
//        authCtrl.logout(router);
//        authController.sessionCount(router);

        com.imslbd.grossary.controller.AuthController authController = new com.imslbd.grossary.controller.AuthController(vertx, router);
        authController.login(router);
        authController.logout(router);
        authController.currentUser(router);
        authController.isCallAgent(router);

        new GoogleMapController(router);

        //App controller
//        new DashboardController(vertx, router);

//        new GroceryController(vertx, router);
//        new CRUDController(vertx, router);

//        new ContactController(vertx, router);

//        new SiteController(vertx, router);

        //CallCenterSupervisor
        CallCenterSupervisorController callCenterSupervisorController = new CallCenterSupervisorController(vertx, router);
        callCenterSupervisorController.index(vertx, router);

    }

    private void otherwiseController(final Router router) {
        router.get("/").handler(context -> {
            if (WebUtils.isLoggedIn(context.session())) {
                WebUtils.redirect(MyUris.CALL_CENTER_SUPERVISOR.value, context.response());
            } else {
                WebUtils.redirect(Uris.LOGIN.value, context.response());
            }
        });
    }

    private void loginFormController(final Router router) {
        router.get(Uris.LOGIN.value).handler(context -> {
            if (WebUtils.isLoggedIn(context.session())) {
                WebUtils.redirect(MyUris.CALL_CENTER_SUPERVISOR.value, context.response());
                return;
            }
            context.response().headers().set(CONTENT_TYPE, TEXT_HTML);

            context.response().end(
                new PageBuilder("Login")
                    .body(new MyLoginTemplate())
                    .build().render());
        });
    }

    private void registerStaticFileHandlers(final Router router) {
        router.route(Uris.STATIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(MyApp.loadConfig().getString("STATIC_DIRECTORY"))
                .setCachingEnabled(true)
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(3 * 30 * 24 * 60 * 60)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );
        router.route(Uris.PUBLIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(MyApp.loadConfig().getString("PUBLIC_DIRECTORY"))
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(0)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );

        router.route(MyUris.STATIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(MyApp.loadConfig().getString("MY_STATIC_DIRECTORY"))
                .setCachingEnabled(true)
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(3 * 30 * 24 * 60 * 60)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );
        router.route(MyUris.PUBLIC_RESOURCES_PATTERN.value).handler(
            StaticHandler.create(MyApp.loadConfig().getString("MY_PUBLIC_DIRECTORY"))
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(0)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );

        router.route(MyUris.APP_DATA_STATIC_PATTERN.value).handler(
            StaticHandler.create(MyApp.loadConfig().getString("UPLOAD_DIRECTORY_BASE"))
                .setFilesReadOnly(true)
                .setMaxAgeSeconds(0)
                .setIncludeHidden(false)
                .setEnableFSTuning(true)
        );
    }

    private Set<String> publicUris() {
        return Arrays.asList(
            Uris.SESSION_COUNT.value,
            Uris.STATIC_RESOURCES_PATTERN.value,
            Uris.PUBLIC_RESOURCES_PATTERN.value,
            Uris.LOGIN.value,
            Uris.REGISTER.value,
            Uris.EVENT_PUBLISH_FORM.value,
            MyUris.PUBLIC_RESOURCES_PATTERN.value,
            MyUris.STATIC_RESOURCES_PATTERN.value
        )
            .stream()
            .map(uri -> {
                final int index = uri.lastIndexOf('/');
                if (index > 0) {
                    return uri.substring(0, index);
                }
                return uri;
            })
            .collect(Collectors.toSet());
    }
}
