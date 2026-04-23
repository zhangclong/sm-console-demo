package com.uh.console.client.base;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.uh.console.client.domain.ConsoleClientConf;
import com.uh.console.client.domain.response.LoginResponse;
import com.uh.console.client.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ConsoleWebClient {

    private static final Logger log = LoggerFactory.getLogger(ConsoleWebClient.class);

    public static final String WEB_API_PREFIX = "/web-api";
    public static final String INTER_LOGIN_PATH = WEB_API_PREFIX + "/interLogin";

    private final ConsoleClientConf config;
    private String loginToken;

    public ConsoleWebClient(ConsoleClientConf config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public ConsoleClientConf getConfig() {
        return config;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public LoginResponse login() {
        Map<String, String> requestBody = new LinkedHashMap<>();
        requestBody.put("username", config.getUsername());
        requestBody.put("password", config.getPassword());
        String responseText = HttpUtil.postJson(resolve(INTER_LOGIN_PATH), requestBody, null);
        LoginResponse response = JSON.parseObject(responseText, LoginResponse.class);
        if (response == null || response.getToken() == null || response.getToken().isBlank()) {
            throw new IllegalStateException("interLogin failed: " + responseText);
        }
        this.loginToken = response.getToken();
        log.info("console-client login success: user={}, baseUrl={}", config.getUsername(), config.normalizedBaseUrl());
        return response;
    }

    public JSONObject logout() {
        ensureLoggedIn();
        String responseText = HttpUtil.postJson(resolve(WEB_API_PREFIX + "/logout"), new LinkedHashMap<>(), authorizationHeader());
        JSONObject response = JSON.parseObject(responseText);
        this.loginToken = null;
        return response;
    }

    public JSONObject getJson(String path, Map<String, String> params) {
        ensureLoggedIn();
        return JSON.parseObject(HttpUtil.get(resolve(path), params, authorizationHeader()));
    }

    public JSONObject postJson(String path, Object body) {
        ensureLoggedIn();
        return JSON.parseObject(HttpUtil.postJson(resolve(path), body, authorizationHeader()));
    }

    public <T> T get(String path, Map<String, String> params, Type responseType) {
        ensureLoggedIn();
        String responseText = HttpUtil.get(resolve(path), params, authorizationHeader());
        return JSON.parseObject(responseText, responseType);
    }

    public <T> T post(String path, Object body, Type responseType) {
        ensureLoggedIn();
        String responseText = HttpUtil.postJson(resolve(path), body, authorizationHeader());
        return JSON.parseObject(responseText, responseType);
    }

    public String resolve(String path) {
        String normalizedBaseUrl = config.normalizedBaseUrl();
        if (path == null || path.isBlank()) {
            return normalizedBaseUrl;
        }
        return path.startsWith("/") ? normalizedBaseUrl + path : normalizedBaseUrl + "/" + path;
    }

    private void ensureLoggedIn() {
        if (loginToken == null || loginToken.isBlank()) {
            login();
        }
    }

    private String authorizationHeader() {
        return loginToken == null || loginToken.isBlank() ? null : "Bearer " + loginToken;
    }
}

