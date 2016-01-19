package com.imslbd.grossary;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by someone on 12/11/2015.
 */
public class MyApp {
    public static final int MIN_PAGE_SIZE = 5;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String IMAGE_DIRECTORY = "IMAGE_DIRECTORY";
    private static final JsonObject config = loadConfig();
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String CURRENT_PROFILE = "CURRENT_PROFILE";
    private static final String PROFILES = "PROFILES";

    static {
        if (loadConfig().getBoolean("dev-mode")) {
            System.setProperty("dev-mode", "true");
        }
    }

    public static JsonObject loadConfig() {
        try {
            if (config == null) {
                JsonObject config;
                final File file = new File(CONFIG_FILE_NAME);
                if (file.exists()) {
                    config = new JsonObject(FileUtils.readFileToString(file));
                } else {
                    final InputStream inputStream = MyApp.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
                    config = new JsonObject(IOUtils.toString(inputStream));
                }
                return config
                        .getJsonObject(PROFILES, new JsonObject())
                        .getJsonObject(config.getString(CURRENT_PROFILE), new JsonObject());
            } else return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final JsonObject dbConfig() {
        return loadConfig().getJsonObject("database");
    }

    public static void main(String... args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
    }
}
