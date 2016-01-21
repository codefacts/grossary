package com.imslbd.grossary.controller;

import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.util.Util;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by someone on 08/12/2015.
 */
public class Controllers {
    public static final CharSequence APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final CharSequence APPLICATION_JSON = "application/json; charset=UTF-8";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static void exportLoop(Message<Buffer> msg, RoutingContext ctx) {
        try {

            if (!ctx.response().isChunked()) ctx.response().setChunked(true);

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
                exportLoop(message, ctx);
            }).error(ctx::fail);

            System.out.println("CONTROLLER: REPLYING...");
            msg.reply(null, Util.makeDeferred(defer));

        } catch (Exception ex) {
            ex.printStackTrace();
            ctx.fail(ex);
        }
    }
}
