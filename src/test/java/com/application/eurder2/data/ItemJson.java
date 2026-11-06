package com.application.eurder2.data;

import java.util.LinkedHashMap;
import java.util.Map;

/** Builds a JSON body for POST /items. Numbers stay unquoted, strings are quoted. */
public final class ItemJson {
    private final Map<String, Object> fields = new LinkedHashMap<>();

    private ItemJson() {
        fields.put("name", "Laptop");
        fields.put("description", "A fast laptop");
        fields.put("price", 999.99);
        fields.put("stock", 10);
    }

    public static ItemJson valid() {
        return new ItemJson();
    }

    public ItemJson with(String field, Object value) {
        fields.put(field, value);
        return this;
    }

    public ItemJson without(String field) {
        fields.remove(field);
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder("{");
        fields.forEach((key, value) -> {
            if (sb.length() > 1) sb.append(',');
            sb.append('"').append(key).append("\":").append(toJson(value));
        });
        return sb.append('}').toString();
    }

    private static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Number) return value.toString();
        return '"' + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}