package com.imslbd.grossary.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by shahadat on 1/11/16.
 */
public class CsvExporter {
    private final String quoteChar;
    private final String seperator;
    private final String lineSeperator;
    private final Map<String, String> header;

    public CsvExporter(Map<String, String> header) {
        this.header = header;
        quoteChar = "\"";
        seperator = ",";
        lineSeperator = "\n";
    }

    public Buffer writeHeader(final Buffer buffer) {
        final int size = header.size() - 1;
        Iterator<String> iterator = header.values().iterator();
        for (int i = 0; (i < size) && iterator.hasNext(); i++) {
            String label = iterator.next();
            wrap(label, buffer).appendString(seperator);
        }
        if (iterator.hasNext()) {
            wrap(iterator.next(), buffer);
        }
        if (header.size() > 0) {
            buffer.appendString(lineSeperator);
        }
        return buffer;
    }

    public Buffer writeData(List<JsonObject> data, Buffer buffer) {
        data.forEach(js -> writeData(js, buffer));
        return buffer;
    }

    public Buffer writeData(JsonObject data, Buffer buffer) {
        Iterator<String> iterator = header.keySet().iterator();
        final int size = header.size() - 1;
        for (int i = 0; i < size && iterator.hasNext(); i++) {
            String text = minimalWrap(data.getValue(iterator.next()));
            buffer.appendString(text).appendString(seperator);
        }
        if (iterator.hasNext()) {
            String text = minimalWrap(data.getValue(iterator.next()));
            buffer.appendString(text);
        }
        if (header.size() > 0) {
            buffer.appendString(lineSeperator);
        }
        return buffer;
    }

    private String minimalWrap(Object value) {
        return value.getClass() == String.class ? "\"" + escape(value.toString()) + "\"" : value.toString();
    }

    public Buffer wrap(String src, Buffer buffer) {
        return buffer.appendString(quoteChar).appendString(escape(src)).appendString(quoteChar);
    }

    public String escape(String src) {
        return src.replace(quoteChar, "\\" + quoteChar);
    }
}
