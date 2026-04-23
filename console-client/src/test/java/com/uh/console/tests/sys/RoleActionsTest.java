package com.uh.console.tests.sys;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.uh.console.client.domain.model.ConsoleRole;
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
class RoleActionsTest extends BaseConsoleClientIntegrationSupport {

    private ConsoleRole createdRole;

    @AfterAll
    void cleanup() {
        if (createdRole != null && createdRole.getRoleId() != null) {
            try {
                roleActions.deleteRole(createdRole.getRoleId());
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

    @Test
    @Order(1)
    @Tag("readOps")
    void listRoles_shouldReturnAdminRole() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("pageNum", "1");
        params.put("pageSize", "20");
        RowsResponse<ConsoleRole> response = roleActions.listRoles(params);

        assertEquals(200, response.getCode());
        assertNotNull(response.getRows());
        assertFalse(response.getRows().isEmpty());
        assertTrue(response.getRows().stream().anyMatch(role -> "admin".equals(role.getRoleKey())));
    }

    @Test
    @Order(2)
    @Tag("readOps")
    void getRoleInfo_shouldReturnAdminRole() {
        BaseResponse<ConsoleRole> response = roleActions.getRoleInfo(1L);
        assertSuccess(response);
        assertNotNull(response.getData());
        assertEquals("admin", response.getData().getRoleKey());
    }

    @Test
    @Order(3)
    @Tag("readOps")
    void optionselect_shouldReturnRoles() {
        BaseResponse<Object> response = roleActions.optionselect();
        assertSuccess(response);
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof JSONArray);
        assertFalse(((JSONArray) response.getData()).isEmpty());
    }

    @Test
    @Order(10)
    @Tag("writeOps")
    void createRole_shouldSucceed() {
        createdRole = buildRole(newSuffix());
        BaseResponse<Object> response = roleActions.createRole(createdRole);
        assertSuccess(response);

        Long roleId = findRoleIdByRoleName(createdRole.getRoleName());
        assertNotNull(roleId, "created role should be queryable from list API");
        createdRole.setRoleId(roleId);
    }

    @Test
    @Order(11)
    @Tag("writeOps")
    void updateRole_shouldSaveMenuCodes() {
        assertNotNull(createdRole);
        assertNotNull(createdRole.getRoleId());

        createdRole.setRemark("updated by console-client e2e");
        createdRole.setMenuCodes(new String[]{"system:user", "system:role", "system:menu"});
        BaseResponse<Object> response = roleActions.updateRole(createdRole);
        assertSuccess(response);

        JSONObject tree = menuActions.roleMenuTreeselect(createdRole.getRoleId());
        assertSuccess(tree);
        assertJsonArrayNotEmpty(tree.get("menus"), "menus tree should not be empty");
        JSONArray checkedKeys = tree.getJSONArray("checkedKeys");
        assertNotNull(checkedKeys);
        assertTrue(checkedKeys.contains("system:user"));
    }

    @Test
    @Order(12)
    @Tag("writeOps")
    void changeRoleStatus_shouldSucceed() {
        assertNotNull(createdRole);
        BaseResponse<Object> response = roleActions.changeRoleStatus(createdRole.getRoleId(), "1");
        assertSuccess(response);

        BaseResponse<ConsoleRole> info = roleActions.getRoleInfo(createdRole.getRoleId());
        assertSuccess(info);
        assertEquals("1", info.getData().getStatus());
    }

    @Test
    @Order(13)
    @Tag("readOps")
    void createdRole_shouldRemainQueryable() {
        assertNotNull(createdRole);
        Long roleId = findRoleIdByRoleName(createdRole.getRoleName());
        assertEquals(createdRole.getRoleId(), roleId,
                "current backend does not support role deletion cleanly, so the created role should remain queryable");
    }
}

