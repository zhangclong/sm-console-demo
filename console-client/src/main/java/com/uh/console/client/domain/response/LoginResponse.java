package com.uh.console.client.domain.response;

public class LoginResponse extends BaseResponse<Object> {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

