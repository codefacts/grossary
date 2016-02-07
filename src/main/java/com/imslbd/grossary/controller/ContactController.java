package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyEvents;
import com.imslbd.grossary.MyUris;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.promise.intfs.MapToHandler;
import io.crm.promise.intfs.SuccessHandler;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.Converters;
import io.crm.web.util.WebUtils;
import io.crm.web.util.printers.CsvExporter;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.imslbd.grossary.MyEvents.CONTACTS_SUMMARY;
import static com.imslbd.grossary.MyEvents.GROUP_BY_COUNT_CONTACTS;
import static io.crm.util.Util.accept;
import static io.crm.util.Util.apply;
import static io.crm.util.Util.as;
import static io.crm.web.util.WebUtils.*;

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

        Route handler = router.get(MyUris.CONTACTS_GROUP_BY_COUNT.value).handler(ctx ->
            Promises.from(ctx.request().params())
                .map(prms -> prms.contains("export") ? "export"
                    : prms.contains("exportFlat") ? "exportFlat" : Decision.OTHERWISE)
                .decide(dec -> dec)
                .on("exportFlat", val -> Util.<Buffer>send(vertx.eventBus(),
                    GROUP_BY_COUNT_CONTACTS, toJson(ctx.request().params()))
                    .then(m -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE,
                        Controllers.APPLICATION_OCTET_STREAM)
                        .putHeader(Controllers.CONTENT_DISPOSITION,
                            "attachment; filename=export.csv;"))
                    .then(msg -> Controllers.exportLoop(msg, ctx)))

                .on("export", val1 -> Util.<JsonArray>send(vertx.eventBus(),
                    GROUP_BY_COUNT_CONTACTS, toJson(ctx.request().params()))
                    .map(message -> message.body())

                    .then(js -> ctx.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_OCTET_STREAM)
                        .putHeader(Controllers.CONTENT_DISPOSITION, WebUtils.attachmentFilename("summary.csv")))

                    .then(js -> {
                        if (js.size() > 0) {
                            Map<String, String> map = new LinkedHashMap<>();
                            map.put("grocery", "Grocery");
                            map.put("location", "Location");
                            map.put("posNo", "Pos No");
                            map.put("todayCount", "Today");
                            map.put("totalCount", "Total");
                            CsvExporter exporter = new CsvExporter(map);

                            Buffer buffer = Buffer.buffer(1024 * 4);
                            exporter.writeHeader(buffer);
                            exporter.writeData(js.getList(), buffer);
                            ctx.response().end(buffer);

                        } else {
                            ctx.response().end();
                        }
                    })

                    .error(ctx::fail))

                .otherwise(
                    val1 -> Util.<JsonArray>send(vertx.eventBus(), MyEvents.GROUP_BY_COUNT_CONTACTS, WebUtils.toJson(ctx.request().params()))
                        .map(Message::body)
                        .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                        .then(js -> ctx.response().end(js.encodePrettily()))
                        .error(ctx::fail))
                .error(ctx::fail));
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

            boolean export = Converters.toBoolean(ctx.request().params().get("export"));

            ctx.request().params().remove("_");

            Promises.from(export)
                .decide(expt -> expt ? "export" : Decision.OTHERWISE)
                .on("export",
                    v -> Util.<Buffer>send(vertx.eventBus(),
                        MyEvents.FIND_ALL_CONTACTS,
                        toJson(ctx.request().params()))
                        .then(m -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE,
                            Controllers.APPLICATION_OCTET_STREAM)
                            .putHeader(Controllers.CONTENT_DISPOSITION,
                                "attachment; filename=export.csv;"))
                        .then(m -> Controllers.exportLoop(m, ctx))
                        .error(ctx::fail))
                .otherwise(
                    v -> Util.<JsonObject>send(vertx.eventBus(),
                        MyEvents.FIND_ALL_CONTACTS,
                        toJson(ctx.request().params()))
                        .map(message -> message.body())
                        .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                        .then(js -> ctx.response().end(js.encodePrettily()))
                        .error(ctx::fail))
                .error(ctx::fail)
            ;
        });
    }

    public void groceries(Router router) {
        router.get(MyUris.CONTACTS_GROCERIES.value).handler(ctx -> {
            Promises.from()
                .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(),
                    MyEvents.FIND_ALL_CONTACTS_GROCERIES,
                    toJson(ctx.request().params())))
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
                    toJson(ctx.request().params())))
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
                    toJson(ctx.request().params())))
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
                    toJson(ctx.request().params())))
                .map(message -> message.body())
                .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                .then(js -> ctx.response().end(js.encodePrettily()))
                .error(ctx::fail);
        });
    }

    public void summary(Router router) {
        router.get(MyUris.CONTACTS_SUMMARY.value).handler(ctx ->
            Promises.from()
                .mapToPromise(val1 -> Util.<JsonObject>send(vertx.eventBus(),
                    CONTACTS_SUMMARY,
                    toJson(ctx.request().params()))
                    .map(message -> message.body())
                    .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(js -> ctx.response().end(js.encodePrettily())))
                .error(ctx::fail));
    }

    public void summaryDetails(Router router) {
        router.get(MyUris.CONTACTS_SUMMARY_DETAILS.value).handler(ctx -> {
            Promises.from(ctx.request().params())
                .then(prms -> prms.remove("_"))
                .map(prms -> prms.contains("export"))
                .decide(val -> val ? "export" : Decision.OTHERWISE)
                .on("export", val1 -> Util.<JsonArray>send(vertx.eventBus(),
                    MyEvents.CONTACTS_SUMMARY_DETAILS,
                    toJson(ctx.request().params()))
                    .map(Message::body)
                    .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_OCTET_STREAM)
                        .putHeader(Controllers.CONTENT_DISPOSITION, WebUtils.attachmentFilename("summary.csv")))
                    .map(js -> {
                        Map map = new JsonObject()
                            .put("grocery", "Grocery")
                            .put("location", "Location")
                            .put("posNo", "Pos No")
                            .put("date", "Date")
                            .put("totalCount", "Total").getMap();

                        CsvExporter csvExporter = new CsvExporter(map);

                        Buffer buffer = Buffer.buffer(1024 * 4);
                        csvExporter.writeHeader(buffer);
                        accept(((List<JsonObject>) js.getList()), list ->
                            list.forEach(jso -> {
                                ExceptionUtil.toRuntime(() ->
                                    jso.put("date", Util.formatDate(Util.parseIsoDate(jso.getString("date")), "")));
                                csvExporter.writeData(jso, buffer);
                            }));
                        return buffer;
                    })
                    .then(buffer -> ctx.response().end((Buffer) buffer))
                    .error(ctx::fail))
                .otherwise(val2 -> Util.<JsonArray>send(vertx.eventBus(),
                    MyEvents.CONTACTS_SUMMARY_DETAILS,
                    toJson(ctx.request().params()))
                    .map(Message::body)
                    .then(js -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(js -> ctx.response().end(js.encodePrettily()))
                    .error(ctx::fail))
                .error(ctx::fail);
        });
    }
}
