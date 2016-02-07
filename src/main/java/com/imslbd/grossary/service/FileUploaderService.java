package com.imslbd.grossary.service;

import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Date;

import static io.crm.util.Util.parseMongoDate;
import static io.crm.util.Util.toIsoString;

/**
 * Created by shahadat on 12/30/15.
 */
public class FileUploaderService {
    private final Vertx vertx;

    public FileUploaderService(Vertx vertx) {
        this.vertx = vertx;
    }

    public void saveByteArray(Message<Buffer> message) {
        saveByteArray(message.headers().get("path"), message.body())
            .then(v -> message.reply(null))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    private Promise<Void> saveByteArray(String path, Buffer buffer) {
        Defer<Void> defer = Promises.defer();
        vertx.fileSystem().createFile(path, Util.makeDeferred(defer));
        return defer.promise()
            .mapToPromise(v -> {
                Defer<Void> defer1 = Promises.defer();
                vertx.fileSystem().writeFile(path, buffer, Util.makeDeferred(defer1));
                return defer1.promise().error(e -> e.printStackTrace())
                    .then(s ->
                        System.out.println("Done FILE"));
            });
    }

    public static void main(String... args) {
        System.out.println(parseMongoDate("2016-01-25T07:08:57Z", null));
        System.out.println(toIsoString(new Date()));
    }
}
