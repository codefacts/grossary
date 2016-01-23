package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.ss;
import com.imslbd.grossary.util.StatusRange;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.util.Util;
import io.crm.web.ApiEvents;
import io.crm.web.ST;
import io.crm.web.Uris;
import io.crm.web.util.WebUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by shahadat on 1/23/16.
 */
public class AuthController {
    final Vertx vertx;

    public AuthController(Vertx vertx, Router router) {
        this.vertx = vertx;
    }

    public void login(final Router router) {
        router.post(Uris.LOGIN.value).handler(BodyHandler.create());
        router.post(Uris.LOGIN.value).handler(ctx -> Promises.from()
            .mapToPromise(v -> Util.<JsonObject>send(vertx.eventBus(), ApiEvents.LOGIN_API,
                WebUtils.toJson(ctx.request().params())))
            .map(m -> m.body())
            .decideAndMap(js -> Decision.of(js.getInteger("statusCode", 1) >= StatusRange.ERROR_STARTS ? "Error" : Decision.OTHERWISE, js))
            .on("Error", val -> ctx.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end(val.encodePrettily()))
            .otherwise(user -> {
                ctx.session().put(ST.currentUser, user);
                ctx.response().end(ST.ok);
            })
            .error(ctx::fail));
    }

    public void currentUser(Router router) {
        router.get(MyUris.CURRENT_USER.value).handler(ctx -> {
            Promises.from()
                .then(v -> {
                    ctx.response().end(((JsonObject) ctx.session().get(ss.currentUser)).encodePrettily());
                })
                .error(ctx::fail);
        });
    }
}
