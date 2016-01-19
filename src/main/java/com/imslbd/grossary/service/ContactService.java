package com.imslbd.grossary.service;

import com.imslbd.grossary.MyApp;
import com.imslbd.grossary.util.CsvExporter;
import com.imslbd.grossary.util.MyUtil;
import com.imslbd.grossary.util.SqlUtils;
import io.crm.promise.Decision;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.util.touple.immutable.Tpl2;
import io.crm.util.touple.immutable.Tpl3;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.imslbd.grossary.util.MyUtil.parseDate;
import static com.imslbd.grossary.util.SqlUtils.escf;
import static io.crm.util.Util.apply;
import static io.crm.util.Util.toIsoString;
import static io.crm.web.util.WebUtils.query;

/**
 * Created by shahadat on 1/10/16.
 */
public class ContactService {
    private final Vertx vertx;
    private final JDBCClient jdbcClient;

    public ContactService(Vertx vertx, JDBCClient jdbcClient) {
        this.vertx = vertx;
        this.jdbcClient = jdbcClient;
    }

    public void findAllContactsGroceries(Message<JsonObject> message) {
        final String COL_GROCERY = "grocery";
        Defer<SQLConnection> defer = Promises.defer();
        jdbcClient.getConnection(Util.makeDeferred(defer));
        defer.promise()
            .mapToPromise(con -> {

                JsonObject js = message.body();
                List<String> stringList = js.stream().map(e -> e.getKey() + " = ?").collect(Collectors.toList());
                String where = apply(String.join(" and ", stringList),
                    w -> w.trim().isEmpty() ? "" : "where " + w);

                Defer<ResultSet> resultSetDefer = Promises.defer();
                con.queryWithParams("SELECT DISTINCT " + COL_GROCERY + " FROM `contacts` " + where,
                    new JsonArray(js.getMap().values().stream()
                        .map(s -> String.valueOf(s)).collect(Collectors.toList())), Util.makeDeferred(resultSetDefer));

                return resultSetDefer.promise().complete(p -> con.close());
            })
            .map(resultSet -> resultSet.getRows().stream().map(js -> js.getString(COL_GROCERY)).collect(Collectors.toList()))
            .map(strings -> new JsonArray(strings))
            .then(rs -> message.reply(new JsonObject().put("data", rs)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findAllContactsLocations(Message<JsonObject> message) {
        final String COL_GROCERY = "location";
        Defer<SQLConnection> defer = Promises.defer();
        jdbcClient.getConnection(Util.makeDeferred(defer));
        defer.promise()
            .mapToPromise(con -> {

                JsonObject js = message.body();
                List<String> stringList = js.stream().map(e -> e.getKey() + " = ?").collect(Collectors.toList());
                String where = apply(String.join(" and ", stringList),
                    w -> w.trim().isEmpty() ? "" : "where " + w);

                Defer<ResultSet> resultSetDefer = Promises.defer();
                con.queryWithParams("SELECT DISTINCT " + COL_GROCERY + " FROM `contacts` " + where,
                    new JsonArray(js.getMap().values().stream()
                        .map(s -> String.valueOf(s)).collect(Collectors.toList())), Util.makeDeferred(resultSetDefer));

                return resultSetDefer.promise().complete(p -> con.close());
            })
            .map(resultSet -> resultSet.getRows().stream().map(js -> js.getString(COL_GROCERY)).collect(Collectors.toList()))
            .map(strings -> new JsonArray(strings))
            .then(rs -> message.reply(new JsonObject().put("data", rs)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findAllContactsPosNos(Message<JsonObject> message) {
        final String COL_GROCERY = "posNo";
        Defer<SQLConnection> defer = Promises.defer();
        jdbcClient.getConnection(Util.makeDeferred(defer));
        defer.promise()
            .mapToPromise(con -> {

                JsonObject js = message.body();
                List<String> stringList = js.stream().map(e -> e.getKey() + " = ?").collect(Collectors.toList());
                String where = apply(String.join(" and ", stringList),
                    w -> w.trim().isEmpty() ? "" : "where " + w);

                Defer<ResultSet> resultSetDefer = Promises.defer();
                con.queryWithParams("SELECT DISTINCT " + COL_GROCERY + " FROM `contacts` " + where,
                    new JsonArray(js.getMap().values().stream()
                        .map(s -> String.valueOf(s)).collect(Collectors.toList())), Util.makeDeferred(resultSetDefer));

                return resultSetDefer.promise().complete(p -> con.close());
            })
            .map(resultSet -> resultSet.getRows().stream().map(js -> js.getString(COL_GROCERY)).collect(Collectors.toList()))
            .map(strings -> new JsonArray(strings))
            .then(rs -> message.reply(new JsonObject().put("data", rs)))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findContactsDateMinMax(Message<JsonObject> message) {
        final String COL_DATE = "date";
        Defer<SQLConnection> defer = Promises.defer();
        jdbcClient.getConnection(Util.makeDeferred(defer));
        defer.promise()
            .mapToPromise(con -> {

                JsonObject js = message.body();
                List<String> stringList = js.stream().map(e -> e.getKey() + " = ?").collect(Collectors.toList());
                String where = apply(String.join(" and ", stringList),
                    w -> w.trim().isEmpty() ? "" : "where " + w);

                Defer<ResultSet> resultSetDefer = Promises.defer();
                con.queryWithParams("SELECT MIN(`" + COL_DATE + "`) as minDate, MAX(`" + COL_DATE + "`) as maxDate FROM `contacts` " + where,
                    new JsonArray(js.getMap().values().stream()
                        .map(s -> String.valueOf(s)).collect(Collectors.toList())), Util.makeDeferred(resultSetDefer));

                return resultSetDefer.promise().complete(p -> con.close());
            })
            .map(resultSet -> resultSet.getRows().stream().findFirst().orElse(Util.EMPTY_JSON_OBJECT))
            .then(rs -> message.reply(rs))
            .error(e -> ExceptionUtil.fail(message, e))
        ;
    }

    public void findContacts(Message<JsonObject> message) {

        Promises.from(sqlFindAllContacts(message))
            .decideAndMap(tpl -> Decision.of((tpl.t3 ? "export" : Decision.OTHERWISE), tpl.dl()))
            .on("export", tpl21 -> export(tpl21, message)
                .error(e -> ExceptionUtil.fail(message, e)))
            .otherwise(data -> Promises.from(data)
                .mapToPromise(tpl -> {
                    Defer<SQLConnection> defer = Promises.defer();
                    jdbcClient.getConnection(Util.makeDeferred(defer));
                    return defer.promise().map(con -> Promises.from()
                        .map(v -> tpl.af(con)).error(e -> con.close()).get());
                })
                .mapToPromise(value -> value.apply((con, sql, params) -> {

                    Defer<ResultSet> rs = Promises.defer();

                    con.queryWithParams(sql, params, Util.makeDeferred(rs));

                    return rs.promise().complete(p -> con.close());
                }))
                .map(resultSet -> resultSet.getRows())
                .then(rs -> message.reply(new JsonObject().put("data", rs)))
                .error(e -> ExceptionUtil.fail(message, e)))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    private Promise<Tpl6<ResultSet, Integer,
        Integer, SQLConnection, String, Message<JsonObject>>> export(Tpl2<String, JsonArray> tpl21, Message<JsonObject> message) {
        return Promises.from(tpl21)
            .mapToPromise(tpl -> tpl.apply((sql, params) -> {
                String tempTable = "temp_table_" + new Random().ints().map(n -> (n < 0) ? -n : n).findFirst().getAsInt();
                String Q = "CREATE TEMPORARY TABLE IF NOT EXISTS " + tempTable + " AS (" + sql + ")";

                Defer<SQLConnection> defer = Promises.defer();
                jdbcClient.getConnection(Util.makeDeferred(defer));
                return defer.promise().map(con -> Promises.from().map(v -> Tpls.of(con, Q, params, tempTable)).error(e -> con.close()).get());
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

                con.query("select * from " + tempTable + " limit 1, " + pageSize, Util.makeDeferred(rs));

                return rs.promise().map(resultSet -> Tpls.of(
                    resultSet, 1, pageSize, con, tempTable, message)).error(e -> con.close());

            }))
            .mapToPromise(value -> retrieveAndReply(Promises.from(value)));
    }

    private Promise<Tpl6<ResultSet, Integer,
        Integer, SQLConnection, String, Message<JsonObject>>>
    retrieveAndReply(Promise<
        Tpl6<ResultSet, Integer, Integer, SQLConnection,
            String, Message<JsonObject>>> from) {

        return from.map(tpl -> tpl.apply((rs, offset, size, con, tempTable, msg) -> {

            Buffer buffer = Buffer.buffer(1024 * 10);

            CsvExporter csvExporter = new CsvExporter(rs.getColumnNames().stream()
                .collect(Collectors.toMap(s -> s, s -> WebUtils.toTitle(s))));


            csvExporter.writeHeader(buffer);

            csvExporter.writeData(rs.getRows(), buffer);

            return Tpls.of(buffer, offset + size, size, con, tempTable, msg);

        })).mapToPromise(val1 -> val1.apply((buffer, offset, size, con, tmpTable, msg) -> {

            Defer<Message<Void>> defer1 = Promises.defer();
            Defer<ResultSet> defer2 = Promises.defer();

            vertx.setTimer(2, aLong -> {
                Defer<ResultSet> defer = Promises.defer();
                con.query("select * from " + tmpTable + " limit " + offset + ", " + size, Util.makeDeferred(defer));
                defer.promise().then(resSet -> defer2.complete(resSet)).error(defer2::fail);
            });

            System.out.println("SERVICE: REPLYING... >> " + buffer.length());
            msg.reply(buffer, Util.makeDeferred(defer1));

            return Promises.when(defer1.promise(), defer2.promise()).error(e -> con.close())
                .map(tpl2 -> tpl2.apply(
                    (message, resultSet) -> Tpls.<ResultSet, Integer, Integer, SQLConnection, String, Message>
                        of(resultSet, offset, size, con, tmpTable, message)));  //Send the new message throwing away the old one

        })).mapToPromise((val) -> val.apply((resultSet, offset, size, con, tmpTable, msg) -> {
            if (resultSet.getNumRows() <= 0) {
                System.out.println("SERVICE: REPLYING... >> NULL");
                msg.reply(null);    //End conversation
                con.close();
                return Promises.from(null);
            }

            return retrieveAndReply(Promises.from(Tpls.of(
                resultSet, offset, size, con, tmpTable, msg)));
        }));
    }

    private Tpl3<String, JsonArray, Boolean> sqlFindAllContacts(Message<JsonObject> message) {
        JsonObject js = apply(message.body(), entries -> {
            entries.remove("_");
            return new JsonObject(entries.stream()
                .filter(e -> e.getValue() != null
                    && !e.getValue().toString().trim().isEmpty())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
        });

        boolean export = Boolean.parseBoolean(js.getString("export"));
        js.remove("export");

        String date = js.getString("date", "");
        js.remove("date");

        List<String> stringList = js.stream().map(e -> e.getKey() + " = ?").collect(Collectors.toList());

        JsonArray dbArgs = new JsonArray(js.getMap().values().stream()
            .map(s -> String.valueOf(s)).collect(Collectors.toList()));

        String dateCond = MyUtil.splitPair(date, ":").apply((s1, s2) -> {
            if (!s1.trim().isEmpty() && !s2.trim().isEmpty()) {
                dbArgs.add(toIsoString(parseDate(s1)));
                dbArgs.add(toIsoString(parseDate(s2)));
                return "`date` between ? and ?";
            } else if (!s1.trim().isEmpty()) {
                dbArgs.add(toIsoString(parseDate(s1)));
                return "`date` >= ?";
            } else if (!s2.trim().isEmpty()) {
                dbArgs.add(toIsoString(parseDate(s2)));
                return "`date` <= ?";
            } else {
                return "";
            }
        });

        if (!dateCond.isEmpty()) stringList.add(dateCond);

        String where = apply(String.join(" and ", stringList),
            w -> w.trim().isEmpty() ? "" : "where " + w);

        String sql = "SELECT * FROM `contacts` " + where;
        return Tpls.of(sql, dbArgs, export);
    }

    public void countByDate(Message<JsonObject> message) {
        Promises.from(message)
            .map(cn -> {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -15);
                Date start = calendar.getTime();
                Date now = new Date();
                return Tpls.of(start, now);
            })
            .mapToPromise(value -> value.apply((from, to) ->
                query("SELECT COUNT(id) as dataCount, `date` FROM `contacts` WHERE " + SqlUtils.dateBetween("`date`", from, to) + " GROUP BY `date`", jdbcClient)))
            .then(rs -> message.reply(new JsonArray(rs.getRows())))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void contactsSummary(Message<JsonObject> message) {
        Promises.when(
            WebUtils.query("select count(*) as totalCount from contacts", jdbcClient),
            WebUtils.query("select count(id) as todayCount from contacts where " + SqlUtils.dateBetweenToday("`date`"), jdbcClient))
            .then(tpl2 -> tpl2.accept((resultSet, resultSet2) -> {
                message.reply(
                    new JsonObject()
                        .put("totalCount", resultSet.getRows().get(0).getLong("totalCount"))
                        .put("todayCount", resultSet2.getRows().get(0).getLong("todayCount")));
            }))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void groupByCount(Message<JsonObject> message) {
        final String today = SqlUtils.toIsoDateString(new Date());

        Promises
            .when(
                WebUtils.query("select grocery, location, posNo, count(id) as totalCount " +
                    "from contacts " +
                    "group by grocery, location, posNo " +
                    "order by grocery, location, posNo ", jdbcClient),
                WebUtils.query("select grocery, location, posNo, " + escf("date") + ", count(*) as todayCount " +
                    "from contacts where " + SqlUtils.dateBetweenToday(escf("date")) + " " +
                    "group by grocery, location, posNo, " + escf("date"), jdbcClient))
            .map(tpl2 -> tpl2.apply((resultSet, resultSet2) -> {

                Map<String, JsonObject> map = resultSet2.getRows().stream().collect(Collectors
                    .toMap(j -> key(j.getString("grocery"), j.getString("location"), j.getString("posNo")), j -> j));

                return resultSet.getRows().stream()
                    .map(j -> {
                        JsonObject jsdef = map.getOrDefault(key(j.getString("grocery"),
                            j.getString("location"), j.getString("posNo")), Util.EMPTY_JSON_OBJECT);

                        return j.put("todayCount", jsdef.getLong("todayCount", 0L));
                    }).collect(Collectors.toList());
            }))
            .then(list -> message.reply(new JsonArray(list)))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public static String key(String grocery, String location, String posNo) {
        return grocery.trim() + "-" + location + "-" + posNo;
    }

    public void summaryDetails(Message<JsonObject> message) {
        Promises.from(message.body())
            .map(js -> Tpls.of(js.getMap().keySet().stream().map(k -> escf(k) + " = ?").collect(Collectors.joining(" and ")),
                js.getMap().values().stream().collect(Collectors.toList())))
            .mapToPromise(params -> params.apply((where, objects) -> WebUtils.queryWithParams(
                "select grocery, location, posNo, " + escf("date") + ", count(id) as totalCount " +
                    "from contacts " +
                    (where.trim().isEmpty() ? "" : "where " + where) +
                    "group by grocery, location, posNo, " + escf("date") + " " +
                    "order by grocery, location, posNo, " + escf("date") + " ", new JsonArray(objects), jdbcClient)
                .then(rs -> message.reply(new JsonArray(rs.getRows())))
                .error(e ->
                    ExceptionUtil.fail(message, e))));
    }
}
