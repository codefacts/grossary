package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyEvents;
import com.imslbd.grossary.MyUris;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;

/**
 * Created by shahadat on 1/30/16.
 */
public class CallCenterSupervisorController {
    public CallCenterSupervisorController(Vertx vertx, Router router) {

    }

    public void index(Vertx vertx, Router router) {
        router.get(MyUris.CALL_CENTER_SUPERVISOR.value).handler(ctx -> {
            Util.<JsonArray>send(vertx.eventBus(), MyEvents.CALL_CENTER_SUPERVISOR_QUERY,
                WebUtils.toJson(ctx.request().params()))
                .map(Message::body)
                .then(js -> {
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, Controllers.APPLICATION_JSON);
                    ctx.response().end(js.encodePrettily());
                })
                .error(ctx::fail)
            ;
        });
    }
}
