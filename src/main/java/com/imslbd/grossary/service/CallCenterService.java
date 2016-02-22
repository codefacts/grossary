package com.imslbd.grossary.service;

import com.imslbd.grossary.ss;
import io.crm.promise.Promises;
import io.crm.promise.intfs.Defer;
import io.crm.promise.intfs.Promise;
import io.crm.util.ExceptionUtil;
import io.crm.util.Util;
import io.crm.web.util.Pagination;
import io.crm.web.util.WebUtils;
import io.crm.web.util.sql.SqlUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jooq.lambda.Seq;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.crm.web.util.WebUtils.offset;

/**
 * Created by shahadat on 1/30/16.
 */
public class CallCenterService {
    private final HttpClient httpClient;

    public CallCenterService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void regionWiseCallSummary(Message<JsonObject> message) {

        String from = "FROM (((((SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID) " +
            "INNER JOIN BRS ON SMS_INBOX.BR_ID = BRS.BR_ID) " +
            "INNER JOIN DISTRIBUTION_HOUSES ON BRS.DISTRIBUTION_HOUSE_ID = DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID) " +
            "INNER JOIN AREAS ON DISTRIBUTION_HOUSES.AREA_ID = AREAS.AREA_ID) " +
            "INNER JOIN REGIONS ON AREAS.REGION_ID = REGIONS.REGION_ID) " +
            "WHERE SMS_INBOX.DATASOURCE_ID = 2 " +
            "GROUP BY REGIONS.REGION_ID, REGIONS.REGION_NAME " +
            "ORDER BY REGIONS.REGION_ID, REGIONS.REGION_NAME";

        query("SELECT " +
            "REGIONS.REGION_ID, REGIONS.REGION_NAME, " +
            "Count(SMS_INBOX.SMS_ID) AS data_count, " +
            "Count(CALLS.CALL_ID) AS call_count, " +
            "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS success_count " + from)
            .then(b -> message.reply(new JsonObject().put("data", new JsonArray(b.toString()))))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void areaWiseCallSummary(Message<JsonObject> message) {

        final JsonArray eq = Util.or(message.body(), Util.EMPTY_JSON_OBJECT).getJsonArray("eq", Util.EMPTY_JSON_ARRAY);

        eq.add(new JsonObject().put("name", "SMS_INBOX.DATASOURCE_ID").put("value", 2));

        List<JsonObject> params = SqlUtil.params(eq.getList().stream());
        String where = SqlUtil.eq(eq.getList().stream());

        String from =
            "FROM ((((SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID) " +
                "INNER JOIN BRS ON SMS_INBOX.BR_ID = BRS.BR_ID) " +
                "INNER JOIN DISTRIBUTION_HOUSES ON BRS.DISTRIBUTION_HOUSE_ID = DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID) " +
                "INNER JOIN AREAS ON DISTRIBUTION_HOUSES.AREA_ID = AREAS.AREA_ID) " +
                (where.isEmpty() ? "" : "WHERE " + where) + " " +
                "GROUP BY AREAS.AREA_ID, AREAS.AREA_NAME " +
                "ORDER BY AREAS.AREA_ID, AREAS.AREA_NAME";


        query("SELECT " +
            "AREAS.AREA_ID, AREAS.AREA_NAME, " +
            "Count(SMS_INBOX.SMS_ID) AS data_count, " +
            "Count(CALLS.CALL_ID) AS call_count, " +
            "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS success_count " + from, params, 0, 200)
            .then(b -> message.reply(
                new JsonObject()
                    .put("data", new JsonArray(b.toString()))))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public void distributionHouseWiseCallSummary(Message<JsonObject> message) {

        final JsonArray eq = Util.or(message.body(), Util.EMPTY_JSON_OBJECT).getJsonArray("eq", Util.EMPTY_JSON_ARRAY);

        eq.add(new JsonObject().put("name", "SMS_INBOX.DATASOURCE_ID").put("value", 2));

        List<JsonObject> params = SqlUtil.params(eq.getList().stream());
        String where = SqlUtil.eq(eq.getList().stream());

        String from = "FROM (((SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID) " +
            "INNER JOIN BRS ON SMS_INBOX.BR_ID = BRS.BR_ID) " +
            "INNER JOIN DISTRIBUTION_HOUSES ON BRS.DISTRIBUTION_HOUSE_ID = DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID) " +
            (where.isEmpty() ? "" : "WHERE " + where) + " " +
            "GROUP BY DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID, DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_NAME " +
            "ORDER BY DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID, DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_NAME";

        query("SELECT " +
            "DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID, DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_NAME, " +
            "Count(SMS_INBOX.SMS_ID) AS data_count, " +
            "Count(CALLS.CALL_ID) AS call_count, " +
            "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS success_count " + from, params, 0, 200)
            .then(b -> message.reply(new JsonObject().put("data", new JsonArray(b.toString()))))
            .error(e -> ExceptionUtil.fail(message, e));

    }

    public void brWiseCallSummary(Message<JsonObject> message) {

        final JsonArray eq = Util.or(message.body(), Util.EMPTY_JSON_OBJECT).getJsonArray("eq", Util.EMPTY_JSON_ARRAY);

        eq.add(new JsonObject().put("name", "SMS_INBOX.DATASOURCE_ID").put("value", 2));

        List<JsonObject> params = SqlUtil.params(eq.getList().stream());
        String where = SqlUtil.eq(eq.getList().stream());

        String groupBy = "GROUP BY BRS.BR_ID, BRS.BR_NAME ";
        String orderBy = "ORDER BY BRS.BR_ID, BRS.BR_NAME";

        String joins = "INNER JOIN DISTRIBUTION_HOUSES ON BRS.DISTRIBUTION_HOUSE_ID = DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID) " +
            "INNER JOIN AREAS ON DISTRIBUTION_HOUSES.AREA_ID = AREAS.AREA_ID) " +
            "INNER JOIN REGIONS ON AREAS.REGION_ID = REGIONS.REGION_ID) ";
        String from =
            "FROM (((((SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID) " +
                "INNER JOIN BRS ON SMS_INBOX.BR_ID = BRS.BR_ID) " + joins +
                (where.isEmpty() ? "" : "WHERE " + where) + " ";

        Integer page = message.body().getInteger("page", 1);
        Integer size = message.body().getInteger("size", 50);

        Promises
            .when(
                query("SELECT " +
                        "BRS.BR_ID, BRS.BR_NAME, " +
                        "Count(SMS_INBOX.SMS_ID) AS data_count, " +
                        "Count(CALLS.CALL_ID) AS call_count, " +
                        "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS success_count " + from + groupBy + orderBy,
                    params, offset(page, size), size),
                query("select COUNT(*) as row_count from (((BRS " + joins + (where.isEmpty() ? "" : "WHERE " + where), params, 0, 2)
            )
            .then(val -> val.accept(
                (buffer, buffer2) -> message.reply(new JsonObject()
                    .put("data", new JsonArray(buffer.toString()))
                    .put("pagination",
                        new Pagination(page, size,
                            new JsonArray(buffer2.toString())
                                .getJsonObject(0)
                                .getLong("row_count", 0L)).toJson()))))
            .error(e -> ExceptionUtil.fail(message, e));
    }

    public static void main(String... args) {
        String str =
            "SELECT " +
                "REGIONS.REGION_NAME, " +
                "Count(SMS_INBOX.SMS_ID) AS data_count, " +
                "Count(CALLS.CALL_ID) AS call_count, " +
                "Count(IIf([CALL_STATUS_ID]=1,True,Null)) AS success_count " +
                "FROM (((((SMS_INBOX LEFT JOIN CALLS ON SMS_INBOX.SMS_ID = CALLS.CALL_ID) " +
                "INNER JOIN BRS ON SMS_INBOX.BR_ID = BRS.BR_ID) " +
                "INNER JOIN DISTRIBUTION_HOUSES ON BRS.DISTRIBUTION_HOUSE_ID = DISTRIBUTION_HOUSES.DISTRIBUTION_HOUSE_ID) " +
                "INNER JOIN AREAS ON DISTRIBUTION_HOUSES.AREA_ID = AREAS.AREA_ID) " +
                "INNER JOIN REGIONS ON AREAS.REGION_ID = REGIONS.REGION_ID) " +
                "GROUP BY REGIONS.REGION_NAME " +
                "ORDER BY REGIONS.REGION_NAME";

        System.out.println(str);
    }

    public Promise<Buffer> query(String sql) {
        Defer<Buffer> defer = Promises.defer();
        httpClient.get("/sql/query?offset=0&size=200&sql=" +
                ExceptionUtil.toRuntimeCall(() ->
                    URLEncoder.encode(sql, StandardCharsets.UTF_8.name())),
            res -> {
                res.bodyHandler(b -> defer.complete(b));
                res.exceptionHandler(defer::fail);
            })
            .sendHead()
            .putHeader(ss.X_Requested_With, Services.CALL_CENTER_JAVA)
            .exceptionHandler(defer::fail)
            .end();
        return defer.promise();
    }

    public Promise<Buffer> query(String sql, List<JsonObject> list, int offset, int size) {
        Defer<Buffer> defer = Promises.defer();
        final String encode = new JsonArray(list).encode();
        httpClient.post("/sql/queryWIthParams?offset=" + offset + "&size=" + size + "&sql=" +
                ExceptionUtil.toRuntimeCall(() ->
                    URLEncoder.encode(sql, StandardCharsets.UTF_8.name())),
            res -> {
                res.bodyHandler(b -> defer.complete(b));
                res.exceptionHandler(defer::fail);
            })
            .exceptionHandler(defer::fail)
            .sendHead()
            .putHeader(HttpHeaders.CONTENT_LENGTH, Util.toString(encode.length()))
            .putHeader(ss.X_Requested_With, Services.CALL_CENTER_JAVA)
            .write(encode)
            .end();
        return defer.promise();
    }

    public Promise<Buffer> query(String sql, int offset, int size) {
        Defer<Buffer> defer = Promises.defer();
        httpClient.get("/sql/query?offset=" + offset + "&size=" + size + "&sql=" +
                ExceptionUtil.toRuntimeCall(() ->
                    URLEncoder.encode(sql, StandardCharsets.UTF_8.name())),
            res -> {
                res.bodyHandler(defer::complete);
                res.exceptionHandler(defer::fail);
            })
            .sendHead()
            .putHeader(ss.X_Requested_With, Services.CALL_CENTER_JAVA)
            .exceptionHandler(defer::fail)
            .end();
        return defer.promise();
    }

    public Promise<Buffer> scalarQuery(String sql, List<JsonObject> list) {
        Defer<Buffer> defer = Promises.defer();
        final String encode = new JsonArray(list).encode();
        httpClient.post("/sql/scalarWithParams?sql=" +
                ExceptionUtil.toRuntimeCall(() ->
                    URLEncoder.encode(sql, StandardCharsets.UTF_8.name())),
            res -> {
                res.bodyHandler(b -> defer.complete(b));
                res.exceptionHandler(defer::fail);
            })
            .exceptionHandler(defer::fail)
            .sendHead()
            .putHeader(HttpHeaders.CONTENT_LENGTH, Util.toString(encode.length()))
            .putHeader(ss.X_Requested_With, Services.CALL_CENTER_JAVA)
            .write(encode)
            .end();
        return defer.promise();
    }

    public Promise<Buffer> scalarQuery(String sql) {
        Defer<Buffer> defer = Promises.defer();
        httpClient.get("/sql/scalar?sql=" +
                ExceptionUtil.toRuntimeCall(() ->
                    URLEncoder.encode(sql, StandardCharsets.UTF_8.name())),
            res -> {
                res.bodyHandler(b -> defer.complete(b));
                res.exceptionHandler(defer::fail);
            })
            .sendHead()
            .putHeader(ss.X_Requested_With, Services.CALL_CENTER_JAVA)
            .exceptionHandler(defer::fail)
            .end();
        return defer.promise();
    }
}
