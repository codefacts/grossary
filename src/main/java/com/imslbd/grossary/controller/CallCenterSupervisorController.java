package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyApp;
import com.imslbd.grossary.MyEvents;
import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.template.page.CallCenterSupervisor;
import com.imslbd.grossary.template.page.PageTmptBuilder;
import io.crm.util.Util;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;

import static com.imslbd.grossary.MyApp.loadConfig;
import static io.crm.util.Util.EMPTY_JSON_OBJECT;
import static io.crm.util.Util.accept;

/**
 * Created by shahadat on 1/30/16.
 */
public class CallCenterSupervisorController {
    public CallCenterSupervisorController(Vertx vertx, Router router) {

    }

    public void index(Vertx vertx, Router router) {
        router.get(MyUris.CALL_CENTER_SUPERVISOR.value).handler(ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML);
            ctx.response().end(
                accept
                    (new PageTmptBuilder("Call Supervisor"), pageTmptBuilder -> {
                        loadConfig().getJsonObject("BABEL_SCRIPTS", EMPTY_JSON_OBJECT)
                            .forEach(e -> pageTmptBuilder.addBabelScript(e.getKey(), e.getValue().toString()));
                        loadConfig().getJsonObject("HEADER_STYLES", EMPTY_JSON_OBJECT)
                            .forEach(e -> pageTmptBuilder.addStyle(e.getKey(), e.getValue().toString()));
                        loadConfig().getJsonObject("HEADER_SCRIPTS", EMPTY_JSON_OBJECT)
                            .forEach(e -> pageTmptBuilder.addScript(e.getKey(), e.getValue().toString()));
                    })
                    .body(new CallCenterSupervisor())
                    .build().render()
            );
        });
    }
}
