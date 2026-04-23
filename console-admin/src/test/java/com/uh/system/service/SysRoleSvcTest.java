package com.uh.system.service;

import com.uh.common.constant.UserConstants;
import com.uh.common.core.tenant.TenantContext;
import com.uh.common.exception.ServiceException;
import com.uh.system.domain.SysRole;
import com.uh.system.service.config.TestAppConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysRoleSvc（角色管理）集成测试。
 * <p>
 * 验证角色查询、唯一性校验、角色增删改等关键业务路径。
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestAppConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysRoleSvcTest {

    private static final long TEST_TENANT_ID = 1L;

    private Long createdRoleId;

    @Autowired
    private SysRoleSvc roleService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TEST_TENANT_ID);
        createdRoleId = null;
    }

    @AfterEach
    void tearDown() {
        if (createdRoleId != null) {
            roleService.deleteRoleById(createdRoleId);
        }
        TenantContext.clear();
    }

    // ---- 角色查询 ----

    @Test
    @Order(1)
    void selectRoleAll_shouldReturnRoles() {
        List<SysRole> roles = roleService.selectRoleAll();
        assertNotNull(roles, "角色列表不应为 null");
        assertFalse(roles.isEmpty(), "初始化后应至少存在一个角色");
    }

    @Test
    @Order(2)
    void selectRoleList_shouldReturnRoles() {
        SysRole query = new SysRole();
        List<SysRole> roles = roleService.selectRoleList(query);
        assertNotNull(roles);
        assertFalse(roles.isEmpty(), "查询所有角色应有结果");
    }

    @Test
    @Order(3)
    void selectRoleById_adminRoleShouldExist() {
        SysRole role = roleService.selectRoleById(1L);
        assertNotNull(role, "角色 ID=1（admin）应存在");
        assertEquals("admin", role.getRoleKey());
    }

    @Test
    @Order(4)
    void selectRoleById_nonExistentShouldReturnNull() {
        SysRole role = roleService.selectRoleById(99999L);
        assertNull(role, "不存在的角色 ID 应返回 null");
    }

    @Test
    @Order(5)
    void selectRolesByUserId_adminUserShouldHaveRoles() {
        List<SysRole> roles = roleService.selectRolesByUserId(1L);
        assertNotNull(roles);
        assertFalse(roles.isEmpty(), "admin 用户应有角色");
    }

    @Test
    @Order(6)
    void selectRolePermissionByUserId_adminShouldHaveAdminPermission() {
        Set<String> permissions = roleService.selectRolePermissionByUserId(1L);
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty(), "admin 用户应有角色权限");
        assertTrue(permissions.contains("admin"), "admin 用户应有 admin 角色权限");
    }

    // ---- 唯一性校验 ----

    @Test
    @Order(7)
    void checkRoleNameUnique_existingRoleNameShouldNotBeUnique() {
        SysRole role = new SysRole();
        role.setRoleName("超级管理员");
        String result = roleService.checkRoleNameUnique(role);
        assertEquals(UserConstants.NOT_UNIQUE, result, "已存在的角色名应返回 NOT_UNIQUE");
    }

    @Test
    @Order(8)
    void checkRoleNameUnique_newRoleNameShouldBeUnique() {
        SysRole role = new SysRole();
        role.setRoleName("新角色_" + System.currentTimeMillis());
        String result = roleService.checkRoleNameUnique(role);
        assertEquals(UserConstants.UNIQUE, result, "新角色名应返回 UNIQUE");
    }

    @Test
    @Order(9)
    void checkRoleKeyUnique_existingKeyAdminShouldNotBeUnique() {
        SysRole role = new SysRole();
        role.setRoleKey("admin");
        String result = roleService.checkRoleKeyUnique(role);
        assertEquals(UserConstants.NOT_UNIQUE, result, "已存在的角色权限字符串应返回 NOT_UNIQUE");
    }

    @Test
    @Order(10)
    void checkRoleKeyUnique_newKeyShouldBeUnique() {
        SysRole role = new SysRole();
        role.setRoleKey("new_role_key_" + System.currentTimeMillis());
        String result = roleService.checkRoleKeyUnique(role);
        assertEquals(UserConstants.UNIQUE, result, "新角色 key 应返回 UNIQUE");
    }

    // ---- 角色保护校验 ----

    @Test
    @Order(11)
    void checkRoleAllowed_adminRoleShouldThrow() {
        SysRole adminRole = new SysRole(1L);
        assertThrows(ServiceException.class, () -> roleService.checkRoleAllowed(adminRole),
                "admin 角色（超管）不允许修改，应抛出 ServiceException");
    }

    @Test
    @Order(12)
    void checkRoleAllowed_regularRoleShouldNotThrow() {
        SysRole regularRole = new SysRole(4L);
        assertDoesNotThrow(() -> roleService.checkRoleAllowed(regularRole),
                "普通角色的校验不应抛出异常");
    }

    // ---- 角色新增 ----

    @Test
    @Order(13)
    void insertRole_shouldSucceedAndBeQueryable() {
        SysRole role = new SysRole();
        role.setRoleName("测试角色_" + System.currentTimeMillis());
        role.setRoleKey("test_role_" + System.currentTimeMillis());
        role.setRoleSort("99");
        role.setStatus("0");
        role.setMenuCodes(new String[]{});

        int rows = roleService.insertRole(role);
        assertTrue(rows > 0, "insertRole 应返回受影响行数 > 0");
        assertNotNull(role.getRoleId(), "插入后 roleId 应被回填");
        createdRoleId = role.getRoleId();

        // 验证可查到
        SysRole found = roleService.selectRoleById(role.getRoleId());
        assertNotNull(found, "插入后应能查到该角色");
        assertEquals(role.getRoleName(), found.getRoleName(), "角色名应与插入时一致");
        roleService.deleteRoleById(role.getRoleId());
        createdRoleId = null;
    }
}
