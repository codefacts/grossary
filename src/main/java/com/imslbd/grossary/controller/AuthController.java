package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.model.UserType;
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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;

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
            .decideAndMap(js -> Decision.of(js.getInteger("statusCode", 1) >= StatusRange.ERROR_STARTS
                ? "Error" : Decision.CONTINUE, js))
            .on("Error", val -> ctx.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                .end(val.encodePrettily()))
            .contnue(user -> {
                ctx.session().put(ST.currentUser, user);
                ctx.session().put(ST.IS_CALL_AGENT, ctx.request().params().get(ST.IS_CALL_AGENT));
                ctx.response().end(dashboardUrl(user));
            })
            .error(ctx::fail));
    }

    public void logout(final Router router) {
        router.get(Uris.LOGOUT.value).handler(context -> {
            Promises.from()
                .then(v -> {
                    context.session().destroy();
                    WebUtils.redirect(Uris.LOGIN.value, context.response());
                })
                .error(context::fail)
            ;
        });
    }

    private String dashboardUrl(JsonObject user) {

        HashMap<Long, String> map = new HashMap<>();
        map.put(UserType.CALL_CENTER_SUPERVISOR.id(), "/callCenterSupervisor");
        map.put(UserType.ADMIN.id(), "/dashboard");

        return map.get(user.getLong("userTypeId"));
    }

    public void isCallAgent(Router router) {
        router.get(MyUris.IS_CALL_AGENT.value).handler(ctx -> {
            ctx.response().end(ctx.session().get(ST.IS_CALL_AGENT).toString());
        });
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
