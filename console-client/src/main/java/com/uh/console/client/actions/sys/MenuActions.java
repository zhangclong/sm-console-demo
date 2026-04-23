package com.uh.console.client.actions.sys;

import com.alibaba.fastjson2.JSONObject;
import com.uh.console.client.base.BaseActions;
import com.uh.console.client.base.ConsoleWebClient;

public class MenuActions extends BaseActions {

    private static final String MENU_BASE = ConsoleWebClient.WEB_API_PREFIX + "/system/menu";

    public MenuActions(ConsoleWebClient client) {
        super(client);
    }

    public JSONObject treeselect() {
        return getJson(MENU_BASE + "/treeselect", null);
    }

    public JSONObject roleMenuTreeselect(Long roleId) {
        return getJson(MENU_BASE + "/roleMenuTreeselect/" + roleId, null);
    }
}

