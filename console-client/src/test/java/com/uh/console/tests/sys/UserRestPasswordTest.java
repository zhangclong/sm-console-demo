package com.uh.console.tests.sys;

import com.uh.console.client.base.ConsoleWebClient;
import com.uh.console.client.domain.model.ConsoleUser;
import com.uh.console.client.domain.response.BaseResponse;
import com.uh.console.client.utils.AESUtils;
import com.uh.console.tests.support.BaseConsoleClientIntegrationSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("defaultEnv")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRestPasswordTest extends BaseConsoleClientIntegrationSupport {

    private ConsoleUser createdUser;
    private final String newPassword = "Password@456";

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
    @Tag("writeOps")
    void createUser_shouldSucceed() {
        createdUser = buildUser(newSuffix());
        createdUser.setUserName("test");
        Long existingUserId = findUserIdByUserName(createdUser.getUserName());
        if (existingUserId != null) {
            BaseResponse<Object> deleteResponse = userActions.deleteUser(existingUserId);
            assertSuccess(deleteResponse);
        }
        BaseResponse<Object> response = userActions.createUser(createdUser);
        assertSuccess(response);
        Long userId = findUserIdByUserName(createdUser.getUserName());
        assertNotNull(userId);
        createdUser.setUserId(userId);
    }

    @Test
    @Order(2)
    @Tag("writeOps")
    void resetPassword_shouldAllowLoginWithNewPassword() {
        assertNotNull(createdUser);
        BaseResponse<Object> response = userActions.resetUserPwd(createdUser.getUserId(), AESUtils.encryptAES(newPassword));
        assertSuccess(response);

        var config = client.getConfig();
        var loginConf = new com.uh.console.client.domain.ConsoleClientConf();
        loginConf.setBaseUrl(config.getBaseUrl());
        loginConf.setUsername(createdUser.getUserName());
        loginConf.setPassword(newPassword);

        ConsoleWebClient tempClient = new ConsoleWebClient(loginConf);
        String token = tempClient.login().getToken();
        assertNotNull(token);
        assertEquals(true, !token.isBlank());
    }

    @Test
    @Order(3)
    @Tag("writeOps")
    void deleteUser_shouldSucceed() {
        assertNotNull(createdUser);
        BaseResponse<Object> response = userActions.deleteUser(createdUser.getUserId());
        assertSuccess(response);
        createdUser.setUserId(null);
    }
}

