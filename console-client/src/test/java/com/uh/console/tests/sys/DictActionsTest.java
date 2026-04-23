package com.uh.console.tests.sys;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.uh.console.client.actions.sys.DictActions;
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
class DictActionsTest extends BaseConsoleClientIntegrationSupport {

    @Test
    @Order(1)
    @Tag("readOps")
    void dictType_shouldReturnEnumDrivenItems() {
        DictActions dictActions = new DictActions(client);
        JSONObject response = dictActions.dictType("sys_normal_disable");

        assertSuccess(response);
        JSONArray data = response.getJSONArray("data");
        assertNotNull(data);
        assertFalse(data.isEmpty());

        JSONObject firstItem = data.getJSONObject(0);
        assertEquals("sys_normal_disable", firstItem.getString("dictType"));
        assertNotNull(firstItem.getString("dictLabel"));
        assertNotNull(firstItem.getString("dictValue"));
    }
}

