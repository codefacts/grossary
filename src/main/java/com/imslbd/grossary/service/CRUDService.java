package com.imslbd.grossary.service;

import com.imslbd.grossary.util.MyUtil;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.MapToHandler;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.util.touple.immutable.Tpl2;
import io.crm.util.touple.immutable.Tpls;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by shahadat on 12/30/15.
 */
public class CRUDService {
    public final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private final Vertx vertx;
    private final JDBCClient jdbcClient;

    public CRUDService(Vertx vertx, JDBCClient jdbcClient) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;
    }

    public void insert(Message<JsonObject> message) {
        insertData(message.body().getString("tableName"), message.body().getJsonObject("data").getMap())
            .then(s ->
                message.reply(s.getLong(0)))
            .error(e ->
                ExceptionUtil.fail(message, e))
            .complete(p ->
                System.out.println(p));
    }

    public Promise<JsonArray> insertData(String table, Map<String, Object> keyValuePairs) {
        String keys = String.join(", ", keyValuePairs.keySet());

        String vals = String.join(", ", keyValuePairs.values().stream()
            .map(v -> {
                if (v instanceof JsonObject) {
                    JsonObject _js = (JsonObject) v;
                    if (_js.containsKey("$date")) {
                        return String.format("'%s'", formatDate(MyUtil.parseDate(_js.getString("$date"))));
                    }
                } else if (v instanceof Map) {
                    Map<String, String> _js = (Map) v;
                    if (_js.containsKey("$date")) {
                        return String.format("'%s'", formatDate(MyUtil.parseDate(_js.get("$date"))));
                    }
                }
                return "?";
            }).collect(Collectors.toList()));

        String insert = String.format("insert into " + table + "(%s) values (%s)", keys, vals);

        JsonArray array = new JsonArray(keyValuePairs.values().stream()
            .filter((Object v) -> !(v instanceof Map || v instanceof JsonObject))
            .collect(Collectors.toList()));

        Defer<SQLConnection> defer = Promises.defer();
        jdbcClient.getConnection(Util.makeDeferred(defer));
        return defer.promise()
            .mapToPromise(con -> {
                Defer<UpdateResult> defer1 = Promises.defer();
                con.updateWithParams(insert, array, Util.makeDeferred(defer1));
                return defer1.promise().map(r -> r.getKeys()).complete(s -> con.close());
            });
    }

    private String formatDate(Date $date) {
        return dateFormat.format($date);
    }

    public void update(Message<JsonObject> message) {

    }

    public void delete(Message<JsonObject> message) {

    }

    public void query(Message<JsonObject> message) {
        Promises.from(message.body())
            .map(js -> {
                List<String> select = js.getJsonArray("select").getList();
                String selectStr = String.join(", ", select);
                String from = js.getString("from");

                return "select " + selectStr + " from " + from;
            })
            .mapToPromise(queryStr -> {
                Defer<SQLConnection> defer = Promises.defer();
                jdbcClient.getConnection(Util.makeDeferred(defer));
                return defer.promise().map((MapToHandler<SQLConnection, Tpl2<SQLConnection, String>>) sqlConnection -> Tpls.of(sqlConnection, queryStr));
            })
            .mapToPromise(tpl -> tpl.apply((con, s) -> {
                Defer<ResultSet> defer = Promises.defer();
                con.query(s, Util.makeDeferred(defer));
                return defer.promise().complete(v -> con.close());
            }))
            .map(ResultSet::getRows)
            .map(list -> new JsonArray(list))
            .map(array -> new JsonObject().put("data", array))
            .then(js -> message.reply(js))
            .error(e -> ExceptionUtil.fail(message, e))
            .complete(p -> System.out.println("COMPLETE QUERY: " + p));
    }

    public static void main(String... arss) {

    }
}
