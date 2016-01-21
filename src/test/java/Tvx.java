import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.LinkedHashMap;

/**
 * Created by shahadat on 1/20/16.
 */
public class Tvx {
    public static void main(String... args) {
        Vertx vertx = Vertx.vertx();
        vertx.eventBus().consumer("test", event -> {

            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("grecery", "grecery");
            map.put("location", "location");
            map.put("posNo", "posNo");
            map.put("totalCount", 0L);

            JsonObject jsonObject = new JsonObject(map).put("todayCount", 0L);
            System.out.println("SENDING: ");
            System.out.println(jsonObject.encode());    // {"grecery":"grecery","location":"location","posNo":"posNo","totalCount":0,"todayCount":0}
            event.reply(jsonObject);
        });
        Router router = Router.router(vertx);
        router.get().handler(ctx -> {
            vertx.eventBus().send("test", null, (AsyncResult<Message<JsonObject>> event) -> {
                JsonObject body = event.result().body();
                System.out.println("RECEIVED: ");
                System.out.println(body.encode());  // {"location":"location","todayCount":0,"grecery":"grecery","posNo":"posNo","totalCount":0}

                ctx.response().end(body.encodePrettily());
            });
        });
        HttpServer server = vertx.createHttpServer().requestHandler(router::accept).listen(8055);
    }
}
