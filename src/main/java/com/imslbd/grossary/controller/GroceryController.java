package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyApp;
import com.imslbd.grossary.MyEvents;
import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.ss;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import static io.crm.util.Util.send;

/**
 * Created by shahadat on 12/30/15.
 */
public class GroceryController {
    public static final String authToken = "kasj4asiuai45aiasdfk2adpas";
    private final Vertx vertx;

    public GroceryController(Vertx vertx, Router router) {
        this.vertx = vertx;
        create(router);

        ExceptionUtil.toRuntime(() -> Files.createDirectories(Paths.get(imageUploadDirectory(""))));
    }

    public void create(Router router) {
        router.post(MyUris.GROCERY.value).handler(BodyHandler.create());
        router.post(MyUris.GROCERY.value).handler(ctx -> {
            String auth = ctx.request().headers().get(HttpHeaders.AUTHORIZATION);
            if (!auth.equals(authToken)) {
                ctx.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
                ctx.response().end("Unauthorized");
            }

            JsonObject js = ctx.getBodyAsJson();

            byte[] bytes = Base64.getDecoder().decode(js.getString("signature"));

            String imageFileName = genImageFileName(js);

            Promise<Message<Object>> promise = send(vertx.eventBus(), MyEvents.SAVE_BYTEARRAY,
                Buffer.buffer(bytes),
                new DeliveryOptions().addHeader("path", imageUploadDirectory(imageFileName)));

            JsonObject sql = new JsonObject()
                .put("tableName", "contacts")
                .put("data", js.put("signature", imageFileName));

            Promise<Message<Object>> promise1 = send(vertx.eventBus(), MyEvents.INSERT, sql);

            Promises.when(promise, promise1)
                .then(v -> ctx.put(HttpHeaders.CONTENT_TYPE.toString(), Controllers.APPLICATION_JSON))
                .then(v -> {
                    ctx.response().end(new JsonObject().put("status", ss.ok).encodePrettily());
                    System.out.println("RESPONSE COMPLETE");
                }).error(ctx::fail);
        });
    }

    public String imageUploadDirectory(String imageFileName) {
        return Paths.get(MyApp.loadConfig().getString("UPLOAD_DIRECTORY_BASE"), "images", imageFileName).toString();
    }

    public String genImageFileName(JsonObject js) {
        return js.getString("phone", "") + "-" + UUID.randomUUID().toString() + ".PNG";
    }
}
