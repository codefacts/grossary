package com.imslbd.grossary.controller;

import com.google.common.net.HttpHeaders;
import com.imslbd.grossary.MyEvents;
import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.ss;
import com.imslbd.grossary.template.SidebarTmpt;
import com.imslbd.grossary.template.page.DashboardTmptBuilder;
import com.imslbd.grossary.template.page.PageTmptBuilder;
import com.imslbd.grossary.template.page.QeuryFormTmpt;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by shahadat on 12/30/15.
 */
public class CRUDController {
    private final Vertx vertx;

    public CRUDController(Vertx vertx, Router router) {
        this.vertx = vertx;
        insert(router);
        insertForm(router);
        query(router);
        queryForm(router);
    }

    public void insert(Router router) {
        router.post(MyUris.INSERT.value).handler(BodyHandler.create());
        router.post(MyUris.INSERT.value).handler(ctx -> {
            Util.send(vertx.eventBus(), MyEvents.INSERT, ctx.getBodyAsJson())
                    .then(v -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(v -> ctx.response().end(new JsonObject().put("status", ss.ok).encodePrettily()))
                    .error(ctx::fail)
            ;
        });
    }

    public void insertForm(Router router) {
        router.get(MyUris.INSERT.value).handler(ctx -> {
            Promises.from()
                    .then(v -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(v -> ctx.response().end(new JsonObject().put("status", ss.ok).encodePrettily()))
                    .error(ctx::fail)
            ;
        });
    }

    public void query(Router router) {
        router.post(MyUris.QUERY.value).handler(BodyHandler.create());
        router.post(MyUris.QUERY.value).handler(ctx -> {
            JsonObject js = ctx.getBodyAsJson();
            Util.<JsonObject>send(vertx.eventBus(), MyEvents.QUERY, js)
                    .map(m -> m.body())
                    .then(r -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON))
                    .then(r -> ctx.response().end(r.encodePrettily()));
        });
    }

    public void queryForm(Router router) {
        router.get(MyUris.QUERY.value).handler(ctx -> {
            ctx.response().end(
                    new PageTmptBuilder("Query")
                            .body(new DashboardTmptBuilder()
                                    .setSidebarTemplate(new SidebarTmpt(ctx.request().path()))
                                    .setContentTemplate(new QeuryFormTmpt())
                                    .build())
                            .build().render()
            );
        });
    }
}
