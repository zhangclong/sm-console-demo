package com.uh.console.client.actions.sys;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.uh.console.client.base.BaseActions;
import com.uh.console.client.base.ConsoleWebClient;
import com.uh.console.client.domain.model.ConsoleUser;
import com.uh.console.client.domain.response.BaseResponse;
import com.uh.console.client.domain.response.RowsResponse;

import java.util.Map;

public class UserActions extends BaseActions {

    private static final String USER_BASE = ConsoleWebClient.WEB_API_PREFIX + "/system/user";

    public UserActions(ConsoleWebClient client) {
        super(client);
    }

    public RowsResponse<ConsoleUser> listUsers(Map<String, String> params) {
        return get(USER_BASE + "/list", params, new TypeReference<RowsResponse<ConsoleUser>>() {}.getType());
    }

    public BaseResponse<ConsoleUser> getUserInfo(Long userId) {
        return get(USER_BASE + "/" + userId, null, new TypeReference<BaseResponse<ConsoleUser>>() {}.getType());
    }

    public BaseResponse<Object> createUser(ConsoleUser user) {
        return post(USER_BASE, user, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> updateUser(ConsoleUser user) {
        return post(USER_BASE + "/edit", user, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> deleteUser(Long... userIds) {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < userIds.length; i++) {
            if (i > 0) {
                ids.append(',');
            }
            ids.append(userIds[i]);
        }
        return get(USER_BASE + "/delete/" + ids, null, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> resetUserPwd(Long userId, String encryptedPassword) {
        JSONObject body = new JSONObject();
        body.put("userId", userId);
        body.put("password", encryptedPassword);
        return post(USER_BASE + "/resetPwd", body, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> changeUserStatus(Long userId, String status) {
        JSONObject body = new JSONObject();
        body.put("userId", userId);
        body.put("status", status);
        return post(USER_BASE + "/changeStatus", body, new TypeReference<BaseResponse<Object>>() {}.getType());
    }
}

