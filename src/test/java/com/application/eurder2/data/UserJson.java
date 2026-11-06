package com.application.eurder2.data;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class UserJson {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final Map<String, Object> fields = new LinkedHashMap<>();
    private final Map<String, Object> address = new LinkedHashMap<>();

    private UserJson() {
        int n = COUNTER.incrementAndGet();
        fields.put("firstname", "Nora");
        fields.put("lastname", "Petry");
        fields.put("email", "new" + n + "@test.com");
        fields.put("password", "Password1!");
        fields.put("phoneNumber", String.format("+3249%07d", n)); // unique, 12 chars
        address.put("street", "Main Street");
        address.put("number", "12A");
        address.put("city", "Brussels");
        address.put("postalCode", "1000");
        address.put("country", "Belgium");
        fields.put("address", address);
    }

    public static UserJson valid() {
        return new UserJson();
    }

    public static UserJson validWithRole(String role) {
        return new UserJson().with("role", role);
    }

    public UserJson with(String field, Object value) {
        fields.put(field, value);
        return this;
    }

    public UserJson withAddress(String field, Object value) {
        address.put(field, value);
        return this;
    }

    public UserJson without(String field) {
        fields.remove(field);
        return this;
    }

    public String email() {
        return (String) fields.get("email");
    }

    public String phone() {
        return (String) fields.get("phoneNumber");
    }

    public String build() {
        return toJson(fields);
    }

    public static String basicAuth(String email, String password) {
        String token = Base64.getEncoder().encodeToString((email + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    @SuppressWarnings("unchecked")
    private static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            ((Map<String, Object>) map).forEach((k, v) -> {
                if (sb.length() > 1) sb.append(',');
                sb.append('"').append(k).append("\":").append(toJson(v));
            });
            return sb.append('}').toString();
        }
        return '"' + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}