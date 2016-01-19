import com.google.common.net.HttpHeaders;
import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.controller.GroceryController;
import com.squareup.okhttp.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by shahadat on 12/30/15.
 */
public class Post {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public String post(String url, String json, String auth) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader(HttpHeaders.AUTHORIZATION, auth)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void main(String[] args) throws Exception {
        String encode = new JsonObject()
//                .put("tableName", "contacts")
//                .put("data", new JsonObject())
                .put("signature", new JsonArray(Arrays.<Byte>asList(new Byte[]{1, 2})))
                .put("phone", "019514428" + (int)(Math.random() * 100)).encode();
        String signature = new Post().post("http://localhost:8085/" + MyUris.GROCERY.value,
                encode,
                GroceryController.authToken);
        System.out.print(signature);
    }
}
