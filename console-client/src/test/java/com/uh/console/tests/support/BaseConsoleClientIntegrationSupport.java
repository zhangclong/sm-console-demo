package com.uh.console.tests.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.uh.console.client.actions.sys.MenuActions;
import com.uh.console.client.actions.sys.RoleActions;
import com.uh.console.client.actions.sys.UserActions;
import com.uh.console.client.base.ConsoleWebClient;
import com.uh.console.client.domain.model.ConsoleRole;
import com.uh.console.client.domain.model.ConsoleUser;
import com.uh.console.client.domain.response.BaseResponse;
import com.uh.console.client.domain.response.RowsResponse;
import com.uh.console.client.utils.AESUtils;
import com.uh.console.utils.TestConfigManager;
import org.junit.jupiter.api.BeforeAll;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseConsoleClientIntegrationSupport {

    protected static final String DEFAULT_TEST_PASSWORD = "Password@123";

    protected ConsoleWebClient client;
    protected UserActions userActions;
    protected RoleActions roleActions;
    protected MenuActions menuActions;

    @BeforeAll
    void setUpClient() {
        client = new ConsoleWebClient(TestConfigManager.getConsoleConfig());
        client.login();
        userActions = new UserActions(client);
        roleActions = new RoleActions(client);
        menuActions = new MenuActions(client);
    }

    protected void assertSuccess(BaseResponse<?> response) {
        assertNotNull(response, "response should not be null");
        assertEquals(200, response.getCode(), () -> "unexpected response: " + response);
    }

    protected void assertSuccess(JSONObject response) {
        assertNotNull(response, "response should not be null");
        assertEquals(200, response.getInteger("code"), () -> "unexpected response: " + response);
    }

    protected ConsoleUser buildUser(String suffix) {
        ConsoleUser user = new ConsoleUser();
        user.setUserName("e2e_user_" + suffix);
        user.setNickName("E2E用户" + suffix);
        user.setEmail(suffix + "@example.com");
        user.setPhonenumber(buildPhoneNumber(suffix));
        user.setSex("0");
        user.setStatus("0");
        user.setRemark("console-client e2e user");
        user.setRoleIds(new Long[]{4L});
        user.setPassword(AESUtils.encryptAES(DEFAULT_TEST_PASSWORD));
        return user;
    }

    protected ConsoleRole buildRole(String suffix) {
        ConsoleRole role = new ConsoleRole();
        role.setRoleName("E2E角色" + suffix);
        role.setRoleKey("e2e:role:" + suffix);
        role.setRoleSort("99");
        role.setStatus("0");
        role.setRemark("console-client e2e role");
        role.setMenuCheckStrictly(false);
        role.setMenuCodes(new String[]{"system:user", "system:role"});
        return role;
    }

    protected String newSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    protected Long findUserIdByUserName(String userName) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("userName", userName);
        params.put("pageNum", "1");
        params.put("pageSize", "20");
        RowsResponse<ConsoleUser> response = userActions.listUsers(params);
        assertEquals(200, response.getCode(), () -> "unexpected listUsers response: " + response);
        List<ConsoleUser> rows = response.getRows();
        assertNotNull(rows, "user rows should not be null");
        return rows.stream()
                .filter(item -> userName.equals(item.getUserName()))
                .map(ConsoleUser::getUserId)
                .findFirst()
                .orElse(null);
    }

    protected Long findRoleIdByRoleName(String roleName) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("roleName", roleName);
        params.put("pageNum", "1");
        params.put("pageSize", "20");
        RowsResponse<ConsoleRole> response = roleActions.listRoles(params);
        assertEquals(200, response.getCode(), () -> "unexpected listRoles response: " + response);
        List<ConsoleRole> rows = response.getRows();
        assertNotNull(rows, "role rows should not be null");
        return rows.stream()
                .filter(item -> roleName.equals(item.getRoleName()))
                .map(ConsoleRole::getRoleId)
                .findFirst()
                .orElse(null);
    }

    protected void assertJsonArrayNotEmpty(Object value, String message) {
        assertTrue(value instanceof JSONArray, message + " (expected JSONArray but was " + value + ")");
        assertTrue(!((JSONArray) value).isEmpty(), message);
    }

    private String buildPhoneNumber(String suffix) {
        String digits = suffix.replaceAll("\\D", "");
        StringBuilder builder = new StringBuilder("13");
        while (builder.length() < 11) {
            if (digits.isEmpty()) {
                digits = "1234567890";
            }
            builder.append(digits.charAt((builder.length() - 2) % digits.length()));
        }
        return builder.substring(0, 11);
    }
}

