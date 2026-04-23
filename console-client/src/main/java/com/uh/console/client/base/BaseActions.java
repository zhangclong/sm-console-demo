package com.uh.console.client.base;

import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class BaseActions {

    private final ConsoleWebClient client;

    protected BaseActions(ConsoleWebClient client) {
        this.client = client;
    }

    protected ConsoleWebClient getClient() {
        return client;
    }

    protected <T> T get(String path, Map<String, String> params, Type responseType) {
        return client.get(path, params, responseType);
    }

    protected <T> T post(String path, Object body, Type responseType) {
        return client.post(path, body, responseType);
    }

    protected JSONObject getJson(String path, Map<String, String> params) {
        return client.getJson(path, params);
    }

    protected JSONObject postJson(String path, Object body) {
        return client.postJson(path, body);
    }
}

