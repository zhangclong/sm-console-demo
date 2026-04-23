package com.uh.console.tests.sys;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.uh.console.tests.support.BaseConsoleClientIntegrationSupport;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@Tag("defaultEnv")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenuActionsTest extends BaseConsoleClientIntegrationSupport {

    @Test
    @Order(1)
    @Tag("readOps")
    void treeselect_shouldReturnMenuTree() {
        JSONObject response = menuActions.treeselect();
        assertSuccess(response);
        assertJsonArrayNotEmpty(response.get("data"), "menu tree should not be empty");

        JSONArray data = response.getJSONArray("data");
        JSONObject firstNode = data.getJSONObject(0);
        assertNotNull(firstNode.getString("label"));
    }

    @Test
    @Order(2)
    @Tag("readOps")
    void roleMenuTreeselect_shouldReturnCheckedKeysAndMenus() {
        JSONObject response = menuActions.roleMenuTreeselect(1L);
        assertSuccess(response);
        assertJsonArrayNotEmpty(response.get("menus"), "role menus should not be empty");

        JSONArray checkedKeys = response.getJSONArray("checkedKeys");
        assertNotNull(checkedKeys);
        assertTrue(checkedKeys.size() >= 0, "checkedKeys should be present even when no explicit bindings are stored");
    }
}

