package com.uh.system.service;

import com.uh.common.core.tenant.TenantContext;
import com.uh.common.exception.ServiceException;
import com.uh.system.domain.SysUser;
import com.uh.system.service.config.TestAppConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysLoginService（用户登录/退出）集成测试。
 * <p>
 * 使用 {@code interfaceLogin()} 接口绕过验证码，直接验证用户名密码。
 * 测试工作目录为 {@code apphome/}（Surefire 已配置），初始用户来自 1.commons.sql。
 * <p>
 * 注意：{@link SysLoginService#interfaceLogin(String, String)} 不经过 Controller 层，
 * 不受 {@code interface.enable} 开关限制（开关仅在 Controller 层生效）。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestAppConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysLoginServiceTest {

    private static final long TEST_TENANT_ID = 1L;
    private static final String[] SEEDED_LOGIN_USERS = {"admin", "sysadmin", "safeadmin", "auditadmin"};
    /**
     * 初始化 SQL 中 admin 用户的明文密码（仅测试使用，与 1.commons.sql 中 BCrypt 哈希对应）。
     * 生产环境的实际密码在部署后应修改，不以此为准。
     */
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SysUserService userService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TEST_TENANT_ID);
        resetSeededLoginUsers();
        // SysLoginService.recordLogin() 需要 HttpServletRequest，通过 RequestContextHolder 提供
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    private void resetSeededLoginUsers() {
        String encodedPassword = com.uh.common.utils.SecurityUtils.encryptPassword(ADMIN_PASSWORD);
        for (String username : SEEDED_LOGIN_USERS) {
            SysUser existing = userService.selectUserByUserName(username);
            assertNotNull(existing, () -> "测试前置数据缺失，未找到用户: " + username);

            userService.resetUserPwd(username, encodedPassword);

            SysUser userToUpdate = new SysUser(existing.getUserId());
            userToUpdate.setLoginRetries(0);
            userToUpdate.setLoginLocked("0");
            userService.updateUser(userToUpdate);
        }
    }

    // ---- 接口登录（interfaceLogin，无验证码）----

    @Test
    @Order(1)
    void interfaceLogin_withValidCredentials_shouldReturnToken() {
        String token = loginService.interfaceLogin("admin", ADMIN_PASSWORD);
        assertNotNull(token, "登录成功应返回非 null 的 token");
        assertFalse(token.isEmpty(), "token 不应为空字符串");
    }

    @Test
    @Order(2)
    void interfaceLogin_withInvalidPassword_shouldThrow() {
        assertThrows(ServiceException.class,
                () -> loginService.interfaceLogin("admin", "wrong_password"),
                "密码错误应抛出 ServiceException");
    }

    @Test
    @Order(3)
    void interfaceLogin_withNonExistentUser_shouldThrow() {
        assertThrows(ServiceException.class,
                () -> loginService.interfaceLogin("non_existent_xyz", ADMIN_PASSWORD),
                "不存在的用户名应抛出 ServiceException");
    }

    @Test
    @Order(4)
    void interfaceLogin_multipleUsers_allShouldSucceed() {
        // 初始化脚本包含多个用户：admin, sysadmin, safeadmin, auditadmin
        String token1 = loginService.interfaceLogin("admin", ADMIN_PASSWORD);
        String token2 = loginService.interfaceLogin("sysadmin", ADMIN_PASSWORD);
        assertNotNull(token1, "admin 登录 token 不应为 null");
        assertNotNull(token2, "sysadmin 登录 token 不应为 null");
    }

    // ---- 密码修改建议 ----

    @Test
    @Order(5)
    void getPasswordSuggestion_adminUserShouldReturnSuggestion() {
        // admin 用户 ID=1，初始化时没有设置密码过期时间（updateTime=null 或有值）
        var result = loginService.getPasswordSuggestion(1L);
        assertNotNull(result, "密码建议结果不应为 null");
        // 结果可以是 "none"、"initial" 或 "expired"，但不为 null
        assertNotNull(result.get("data"), "密码建议数据不应为 null");
    }

    // ---- 用户密码校验 ----

    @Test
    @Order(6)
    void checkUserPassword_withCorrectPassword_shouldNotThrow() {
        assertDoesNotThrow(() -> loginService.checkUserPassword("admin", ADMIN_PASSWORD),
                "正确的密码不应抛出异常");
    }

    @Test
    @Order(7)
    void checkUserPassword_withWrongPassword_shouldThrow() {
        assertThrows(Exception.class,
                () -> loginService.checkUserPassword("admin", "wrong_password"),
                "错误的密码应抛出异常");
    }
}
