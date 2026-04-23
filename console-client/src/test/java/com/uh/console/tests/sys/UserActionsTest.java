package com.uh.console.tests.sys;

import com.uh.console.client.domain.model.ConsoleUser;
import com.uh.console.client.domain.response.BaseResponse;
import com.uh.console.client.domain.response.RowsResponse;
import com.uh.console.tests.support.BaseConsoleClientIntegrationSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("defaultEnv")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserActionsTest extends BaseConsoleClientIntegrationSupport {

    private ConsoleUser createdUser;

    @AfterAll
    void cleanup() {
        if (createdUser != null && createdUser.getUserId() != null) {
            try {
                userActions.deleteUser(createdUser.getUserId());
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

    @Test
    @Order(1)
    @Tag("readOps")
    void listUsers_shouldReturnAdminUser() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("pageNum", "1");
        params.put("pageSize", "20");
        RowsResponse<ConsoleUser> response = userActions.listUsers(params);

        assertEquals(200, response.getCode());
        assertNotNull(response.getRows());
        assertFalse(response.getRows().isEmpty());
        assertTrue(response.getRows().stream().anyMatch(user -> "admin".equals(user.getUserName())));
    }

    @Test
    @Order(2)
    @Tag("readOps")
    void getUserInfo_shouldReturnAdminDetails() {
        BaseResponse<ConsoleUser> response = userActions.getUserInfo(1L);
        assertSuccess(response);
        assertNotNull(response.getData());
        assertEquals("admin", response.getData().getUserName());
    }

    @Test
    @Order(10)
    @Tag("writeOps")
    void createUser_shouldSucceed() {
        createdUser = buildUser(newSuffix());
        BaseResponse<Object> response = userActions.createUser(createdUser);
        assertSuccess(response);

        Long userId = findUserIdByUserName(createdUser.getUserName());
        assertNotNull(userId, "created user should be queryable from list API");
        createdUser.setUserId(userId);
    }

    @Test
    @Order(11)
    @Tag("writeOps")
    void updateUser_shouldSucceed() {
        assertNotNull(createdUser);
        assertNotNull(createdUser.getUserId());

        createdUser.setNickName(createdUser.getNickName() + "-已更新");
        createdUser.setRemark("updated by console-client e2e");
        BaseResponse<Object> response = userActions.updateUser(createdUser);
        assertSuccess(response);

        BaseResponse<ConsoleUser> info = userActions.getUserInfo(createdUser.getUserId());
        assertSuccess(info);
        assertEquals(createdUser.getNickName(), info.getData().getNickName());
    }

    @Test
    @Order(12)
    @Tag("writeOps")
    void changeUserStatus_shouldSucceed() {
        assertNotNull(createdUser);
        BaseResponse<Object> response = userActions.changeUserStatus(createdUser.getUserId(), "1");
        assertSuccess(response);

        BaseResponse<ConsoleUser> info = userActions.getUserInfo(createdUser.getUserId());
        assertSuccess(info);
        assertEquals("1", info.getData().getStatus());
    }

    @Test
    @Order(13)
    @Tag("writeOps")
    void deleteUser_shouldSucceed() {
        assertNotNull(createdUser);
        BaseResponse<Object> response = userActions.deleteUser(createdUser.getUserId());
        assertSuccess(response);

        Long userId = findUserIdByUserName(createdUser.getUserName());
        assertNull(userId, "deleted user should no longer be listed");
        createdUser.setUserId(null);
    }
}

