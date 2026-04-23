package com.uh.console.client.domain;

import java.util.Objects;

public class ConsoleClientConf {

    private String baseUrl;
    private String username;
    private String password;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String normalizedBaseUrl() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("console.baseUrl must not be blank");
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsoleClientConf)) {
            return false;
        }
        ConsoleClientConf that = (ConsoleClientConf) o;
        return Objects.equals(baseUrl, that.baseUrl)
                && Objects.equals(username, that.username)
                && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUrl, username, password);
    }

    @Override
    public String toString() {
        return "ConsoleClientConf{" +
                "baseUrl='" + baseUrl + '\'' +
                ", username='" + username + '\'' +
                "'}";
    }
}

