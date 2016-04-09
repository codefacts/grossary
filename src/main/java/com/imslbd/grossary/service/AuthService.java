package com.imslbd.grossary.service;

import com.imslbd.grossary.StatusCodes;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.util.ExceptionUtil;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Created by shahadat on 1/23/16.
 */
public class AuthService {
    private final Vertx vertx;
    private final JDBCClient jdbcClient;

    public AuthService(Vertx vertx, JDBCClient jdbcClient) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;
    }

    public void login(Message<JsonObject> message) {
        Promises.from(message.body())
            .mapToPromise(auth -> WebUtils.query("select * from users where username = ?",
                new JsonArray().add(auth.getString("username")), jdbcClient))
            .decideAndMap(rs -> Decision.of(rs.getNumRows() < 1 ? "user_not_found" : Decision.OTHERWISE, rs))
            .on("user_not_found", val -> message.reply(
                new JsonObject()
                    .put("statusCode", StatusCodes.USER_NOT_FOUND_ERROR.statusCode())
                    .put("status", StatusCodes.USER_NOT_FOUND_ERROR)
                    .put("message", StatusCodes.USER_NOT_FOUND_ERROR.message())
            ))
            .otherwise(rs -> {
                if (!rs.getRows().get(0).getString("password")
                    .equals(message.body().getString("password"))) {
                    message.reply(new JsonObject()
                        .put("statusCode", StatusCodes.PASSWORD_DOES_NOT_MATCH_ERROR.statusCode())
                        .put("status", StatusCodes.PASSWORD_DOES_NOT_MATCH_ERROR)
                        .put("message", StatusCodes.PASSWORD_DOES_NOT_MATCH_ERROR.message()))
                    ;
                } else {
                    message.reply(rs.getRows().get(0));
                }
            })
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }
}
