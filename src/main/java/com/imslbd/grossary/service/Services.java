package com.imslbd.grossary.service;

import com.imslbd.grossary.MyApp;
import io.crm.util.ExceptionUtil;
import io.crm.web.util.printers.CsvExporter;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.Util;
import io.crm.util.touple.immutable.Tpl2;
import io.crm.util.touple.immutable.Tpl6;
import io.crm.util.touple.immutable.Tpls;
import io.crm.web.util.WebUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.stream.Collectors;

import static io.crm.util.ExceptionUtil.toRuntimeCall;
import static io.crm.util.Util.parseMongoDate;

/**
 * Created by someone on 08/12/2015.
 */
public class Services {

    public static Promise<Tpl6<ResultSet, Integer, Integer, SQLConnection, String, Message<JsonObject>>>
    exportLoop(Tpl2<String, JsonArray> tpl21, Message<JsonObject> message, JDBCClient jdbcClient, Vertx vertx) {
        return Promises.from(tpl21)
            .mapToPromise(tpl -> tpl.apply((sql, params) -> {
                String tempTable = "temp_table_" + new Random().ints().map(n -> (n < 0) ? -n : n).findFirst()
                    .getAsInt();
                String Q = "CREATE TEMPORARY TABLE IF NOT EXISTS " + tempTable + " AS (" + sql + ")";

                Defer<SQLConnection> defer = Promises.defer();
                jdbcClient.getConnection(Util.makeDeferred(defer));
                return defer.promise().map(con -> Promises.from().map(v -> Tpls.of(con, Q, params, tempTable))
                    .error(e -> con.close()).get());
            }))
            .mapToPromise(val -> val.apply((con, Q, params, tmpTable) -> {
                Defer<UpdateResult> rs = Promises.defer();

                con.updateWithParams(Q, params, Util.makeDeferred(rs));

                return rs.promise().error(p -> con.close()).map(updateResult -> Tpls.of(con, updateResult, tmpTable));
            }))
            .mapToPromise(tpl2 -> tpl2.apply((con, updateResult, tempTable) -> {

                System.out.println(updateResult.toJson());

                Defer<ResultSet> rs = Promises.defer();

                Integer pageSize = MyApp.loadConfig().getInteger("EXPORT_PAGE_SIZE");

                con.query("select * from " + tempTable + " limit 0, " + pageSize, Util.makeDeferred(rs));

                return rs.promise().map(resultSet -> Tpls.of(
                    resultSet, 1, pageSize, con, tempTable, message)).error(e -> con.close());

            }))
            .mapToPromise(value -> retrieveAndReply(value, vertx));
    }

    public static Promise<Tpl6<ResultSet, Integer,
        Integer, SQLConnection, String, Message<JsonObject>>>
    retrieveAndReply(
        Tpl6<ResultSet, Integer, Integer, SQLConnection,
            String, Message<JsonObject>> tpl6, Vertx vertx) {

        return Promises.from(tpl6)
            .map(tpl -> tpl.apply((rs, offset, size, con, tempTable, msg) -> {

                Buffer buffer = Buffer.buffer(1024 * 4);

                CsvExporter csvExporter = new CsvExporter(rs.getColumnNames().stream()
                    .collect(Collectors.toMap(s -> s, WebUtils::toTitle, (s, s2) -> s, LinkedHashMap::new)));


                csvExporter.writeHeader(buffer);

                rs.getRows().stream().map(js -> js.put("date", toRuntimeCall(() -> Util.formatDate(parseMongoDate(js.getString("date")), ""))))
                    .forEach(entries -> csvExporter.writeData(entries, buffer));

                return Tpls.of(buffer, offset + size, size, con, tempTable, msg);

            }))
            .mapToPromise(val1 -> val1.apply((buffer, offset, size, con, tmpTable, msg) -> {

                Defer<Message<Void>> defer1 = Promises.defer();
                Defer<ResultSet> defer2 = Promises.defer();

                vertx.setTimer(2, aLong -> {
                    Defer<ResultSet> defer = Promises.defer();
                    con.query("select * from " + tmpTable + " limit " + offset + ", " + size, Util.makeDeferred(defer));
                    defer.promise().then(defer2::complete).error(defer2::fail);
                });

                System.out.println("SERVICE: REPLYING... >> " + buffer.length());
                msg.reply(buffer, Util.makeDeferred(defer1));

                return Promises.when(defer1.promise(), defer2.promise()).error(e -> con.close())
                    .map(tpl2 -> tpl2.apply(
                        (message, resultSet) -> Tpls.<ResultSet, Integer, Integer, SQLConnection, String, Message>
                            of(resultSet, offset, size, con, tmpTable, message)));  //Send the new message throwing away the old one

            }))
            .mapToPromise((val) -> val.apply((resultSet, offset, size, con, tmpTable, msg) -> {
                if (resultSet.getNumRows() <= 0) {
                    System.out.println("SERVICE: REPLYING... >> NULL");
                    msg.reply(null);    //End conversation
                    con.close();
                    return Promises.from(null);
                }

                return retrieveAndReply(Tpls.of(
                    resultSet, offset, size, con, tmpTable, msg), vertx);
            }));
    }
}
