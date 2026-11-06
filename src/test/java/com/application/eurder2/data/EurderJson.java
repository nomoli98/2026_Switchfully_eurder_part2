package com.application.eurder2.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds a JSON body for POST /eurders. */
public final class EurderJson {
    private final List<String> groups = new ArrayList<>();

    private EurderJson() {
    }

    public static EurderJson order() {
        return new EurderJson();
    }

    public static String emptyList() {
        return "{\"itemGroupRequests\":[]}";
    }

    public static String noList() {
        return "{}";
    }

    public EurderJson item(UUID itemId, int amount) {
        groups.add("{\"itemId\":\"" + itemId + "\",\"amount\":" + amount + "}");
        return this;
    }

    public EurderJson itemWithoutId(int amount) {
        groups.add("{\"amount\":" + amount + "}");
        return this;
    }

    public EurderJson itemWithoutAmount(UUID itemId) {
        groups.add("{\"itemId\":\"" + itemId + "\"}");
        return this;
    }

    public EurderJson itemWithRawId(String rawId, int amount) {
        groups.add("{\"itemId\":\"" + rawId + "\",\"amount\":" + amount + "}");
        return this;
    }

    public EurderJson nullItem() {
        groups.add("null");
        return this;
    }

    public String build() {
        return "{\"itemGroupRequests\":[" + String.join(",", groups) + "]}";
    }
}