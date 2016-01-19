package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyEvents;
import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.util.MyUtil;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by shahadat on 1/10/16.
 */
public class ContactController {
    private final Vertx vertx;

    public ContactController(Vertx vertx, Router router) {
        this.vertx = vertx;
        groceries(router);
        locations(router);
        posNos(router);
        dateMinMax(router);
        contacts(router);
        contactsCountByDate(router);
        summary(router);
        groupByCount(router);
        summaryDetails(router);
    }

    private void groupByCount(Router router) {
        router.get(MyUris.CONTACTS_GROUP_BY_COUNT.value).handler(ctx -> {
            Util.<JsonArray>send(vertx.eventBus(), MyEvents.GROUP_BY_COUNT, null)
                .map(Message::body)
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail)
            ;
        });
    }

    private void contactsCountByDate(Router router) {
        router.get(MyUris.CONTACTS_COUNT_BY_DATE.value).handler(ctx -> {
            Util.<JsonArray>send(vertx.eventBus(), MyEvents.CONTACTS_COUNT_BY_DATE, null)
                .map(Message::body)
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail)
            ;
        });
    }

    private void contacts(Router router) {
        router.get(MyUris.CONTACTS.value).handler(ctx -> {
            boolean export = Boolean.parseBoolean(ctx.request().params().get("export"));

            ctx.request().params().remove("/contacts").remove("_");

            if (export) {

                Promises.from()
                    .mapToPromise(v -> Util.<Buffer>send(vertx.eventBus(),
                        MyEvents.FIND_ALL_CONTACTS,
                        WebUtils.toJson(ctx.request().params())))
                    .then(m -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE,
                        Controllers.APPLICATION_OCTET_STREAM)
                        .putHeader(Controllers.CONTENT_DISPOSITION,
                            "attachment; filename=export.csv;").setChunked(true))
                    .then(m -> {
                        respondAndReplyLoop(m, ctx);
                    })
                    .error(ctx::fail);

            } else {
                Promises.from()
                    .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                        MyEvents.FIND_ALL_CONTACTS,
                        WebUtils.toJson(ctx.request().params())))
                    .map(message -> message.body())
                    .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(js -> ctx.response().end(js.encodePrettily()))
                    .error(ctx::fail)
                ;
            }
        });
    }

    private void respondAndReplyLoop(Message<Buffer> msg, RoutingContext ctx) {
        try {
            System.out.println("BUFFER: LENGHT: " + (msg.body() == null ? "NULL" : msg.body().length()));
            if (msg.body() == null || msg.body().length() <= 0) {
                ctx.response().end();
                System.out.println("DONE EXPORT");
                return;
            }

            ctx.response().write(msg.body());

            Defer<Message<Buffer>> defer = Promises.defer();

            defer.promise().then(message -> {
                System.out.println("CONTINUE");
                respondAndReplyLoop(message, ctx);
            }).error(ctx::fail).error(e -> System.out.println(Thread.currentThread()));

            System.out.println("CONTROLLER: REPLYING...");
            msg.reply(null, Util.makeDeferred(defer));

        } catch (Exception ex) {
            ex.printStackTrace();
            ctx.fail(ex);
        }
    }

    public void groceries(Router router) {
        router.get(MyUris.CONTACTS_GROCERIES.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.FIND_ALL_CONTACTS_GROCERIES,
                    WebUtils.toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void locations(Router router) {
        router.get(MyUris.CONTACTS_LOCATIONS.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.FIND_ALL_CONTACTS_LOCATIONS,
                    WebUtils.toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void posNos(Router router) {
        router.get(MyUris.CONTACTS_POS_NOS.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.FIND_ALL_CONTACTS_POS_NOS,
                    WebUtils.toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void dateMinMax(Router router) {
        router.get(MyUris.CONTACTS_DATE_MIN_MAX.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.FIND_CONTACTS_DATE_MIN_MAX,
                    WebUtils.toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void summary(Router router) {
        router.get(MyUris.CONTACTS_SUMMARY.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.CONTACTS_SUMMARY,
                    WebUtils.toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void summaryDetails(Router router) {
        router.get(MyUris.CONTACTS_SUMMARY_DETAILS.value).handler(ctx -> {
            ctx.request().params().remove("_");
            Promises.from()
                .mapToPromise(v -> Util.<JsonArray>send(vertx.eventBus(),
                    MyEvents.CONTACTS_SUMMARY_DETAILS,
                    WebUtils.toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }
}
