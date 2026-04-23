package com.uh.console.client.utils;

import com.alibaba.fastjson2.JSON;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

public final class HttpUtil {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private HttpUtil() {
    }

    public static String get(String url, Map<String, String> params, String authorizationHeader) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(buildUri(url, params))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .header("Accept", "application/json");
        applyAuthorization(builder, authorizationHeader);
        return send(builder.build());
    }

    public static String postJson(String url, Object body, String authorizationHeader) {
        String jsonBody = body == null ? "{}" : (body instanceof String ? (String) body : JSON.toJSONString(body));
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
        applyAuthorization(builder, authorizationHeader);
        return send(builder.build());
    }

    private static URI buildUri(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return URI.create(url);
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        String delimiter = url.contains("?") ? "&" : "?";
        return URI.create(url + delimiter + joiner);
    }

    private static void applyAuthorization(HttpRequest.Builder builder, String authorizationHeader) {
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            builder.header("Authorization", authorizationHeader);
        }
    }

    private static String send(HttpRequest request) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("HTTP request interrupted: " + request.uri(), e);
        }
    }
}

