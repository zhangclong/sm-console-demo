package com.uh.console.client.actions.sys;

import com.alibaba.fastjson2.TypeReference;
import com.uh.console.client.base.BaseActions;
import com.uh.console.client.base.ConsoleWebClient;
import com.uh.console.client.domain.model.ConsoleRole;
import com.uh.console.client.domain.response.BaseResponse;
import com.uh.console.client.domain.response.RowsResponse;

import java.util.Map;

public class RoleActions extends BaseActions {

    private static final String ROLE_BASE = ConsoleWebClient.WEB_API_PREFIX + "/system/role";

    public RoleActions(ConsoleWebClient client) {
        super(client);
    }

    public RowsResponse<ConsoleRole> listRoles(Map<String, String> params) {
        return get(ROLE_BASE + "/list", params, new TypeReference<RowsResponse<ConsoleRole>>() {}.getType());
    }

    public BaseResponse<ConsoleRole> getRoleInfo(Long roleId) {
        return get(ROLE_BASE + "/" + roleId, null, new TypeReference<BaseResponse<ConsoleRole>>() {}.getType());
    }

    public BaseResponse<Object> createRole(ConsoleRole role) {
        return post(ROLE_BASE, role, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> updateRole(ConsoleRole role) {
        return post(ROLE_BASE + "/edit", role, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> deleteRole(Long... roleIds) {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < roleIds.length; i++) {
            if (i > 0) {
                ids.append(',');
            }
            ids.append(roleIds[i]);
        }
        return get(ROLE_BASE + "/delete/" + ids, null, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> changeRoleStatus(Long roleId, String status) {
        ConsoleRole role = new ConsoleRole();
        role.setRoleId(roleId);
        role.setStatus(status);
        return post(ROLE_BASE + "/changeStatus", role, new TypeReference<BaseResponse<Object>>() {}.getType());
    }

    public BaseResponse<Object> optionselect() {
        return get(ROLE_BASE + "/optionselect", null, new TypeReference<BaseResponse<Object>>() {}.getType());
    }
}

