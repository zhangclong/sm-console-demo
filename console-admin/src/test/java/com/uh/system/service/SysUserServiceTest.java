package com.uh.system.service;

import com.uh.common.constant.UserConstants;
import com.uh.common.core.tenant.TenantContext;
import com.uh.system.domain.SysUser;
import com.uh.system.service.config.TestAppConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserService（用户管理）集成测试。
 * <p>
 * 使用真实 MySQL 数据库（若缺失则在测试启动前自动初始化），验证用户查询、
 * 唯一性校验、用户信息更新等关键业务路径。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestAppConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysUserServiceTest {

    private static final long TEST_TENANT_ID = 1L;

    private Long createdUserId;

    @Autowired
    private SysUserService userService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TEST_TENANT_ID);
        createdUserId = null;
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            userService.deleteUserById(createdUserId);
        }
        TenantContext.clear();
    }

    // ---- 用户查询 ----

    @Test
    @Order(1)
    void selectUserList_shouldReturnUsers() {
        SysUser query = new SysUser();
        List<SysUser> users = userService.selectUserList(query);
        assertNotNull(users, "用户列表不应为 null");
        assertFalse(users.isEmpty(), "初始化后应至少存在一名用户");
    }

    @Test
    @Order(2)
    void selectUserByUserName_adminShouldExist() {
        SysUser admin = userService.selectUserByUserName("admin");
        assertNotNull(admin, "admin 用户应存在");
        assertEquals("admin", admin.getUserName());
        assertEquals(1L, admin.getUserId(), "admin 用户 ID 应为 1");
    }

    @Test
    @Order(3)
    void selectUserByUserName_nonExistentUserShouldReturnNull() {
        SysUser user = userService.selectUserByUserName("non_existent_user_xyz");
        assertNull(user, "不存在的用户名应返回 null");
    }

    @Test
    @Order(4)
    void selectUserById_adminShouldBeFound() {
        SysUser user = userService.selectUserById(1L);
        assertNotNull(user, "用户 ID=1 应存在");
        assertEquals("admin", user.getUserName());
    }

    @Test
    @Order(5)
    void selectUserById_nonExistentIdShouldReturnNull() {
        SysUser user = userService.selectUserById(99999L);
        assertNull(user, "不存在的用户 ID 应返回 null");
    }

    @Test
    @Order(6)
    void selectUserList_withUsernameFilter() {
        SysUser query = new SysUser();
        query.setUserName("admin");
        List<SysUser> users = userService.selectUserList(query);
        assertNotNull(users);
        assertFalse(users.isEmpty(), "按用户名过滤应能查到结果");
        assertTrue(users.stream().anyMatch(u -> "admin".equals(u.getUserName())),
                "结果列表应包含 admin 用户");
    }

    // ---- 唯一性校验 ----

    @Test
    @Order(7)
    void checkUserNameUnique_existingUsernameShouldNotBeUnique() {
        SysUser user = new SysUser();
        user.setUserName("admin");
        String result = userService.checkUserNameUnique(user);
        assertEquals(UserConstants.NOT_UNIQUE, result, "已存在的用户名应返回 NOT_UNIQUE");
    }

    @Test
    @Order(8)
    void checkUserNameUnique_newUsernameShouldBeUnique() {
        SysUser user = new SysUser();
        user.setUserName("new_unique_user_" + System.currentTimeMillis());
        String result = userService.checkUserNameUnique(user);
        assertEquals(UserConstants.UNIQUE, result, "新用户名应返回 UNIQUE");
    }

    // ---- 用户角色组 ----

    @Test
    @Order(9)
    void selectUserRoleGroup_adminShouldHaveRoles() {
        String roleGroup = userService.selectUserRoleGroup("admin");
        assertNotNull(roleGroup, "admin 用户的角色组不应为 null");
        assertFalse(roleGroup.isEmpty(), "admin 用户应有至少一个角色");
    }

    // ---- 新增用户 ----

    @Test
    @Order(10)
    void insertAndDeleteUser_shouldSucceed() {
        // 创建测试用户
        SysUser user = new SysUser();
        String testUserName = "test_user_" + System.currentTimeMillis();
        user.setUserName(testUserName);
        user.setNickName("测试用户");
        user.setPassword("$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2");
        user.setStatus("0");
        user.setDelFlag("0");

        int rows = userService.insertUser(user);
        assertTrue(rows > 0, "insertUser 应返回受影响行数 > 0");
        assertNotNull(user.getUserId(), "插入后 userId 应被回填");
        createdUserId = user.getUserId();

        // 验证可查到
        SysUser found = userService.selectUserById(user.getUserId());
        assertNotNull(found, "插入后应能查到该用户");
        assertEquals(testUserName, found.getUserName());

        // 清理：删除测试用户
        int deleted = userService.deleteUserById(user.getUserId());
        assertTrue(deleted > 0, "deleteUserById 应返回受影响行数 > 0");
        createdUserId = null;
    }
}
