package com.uh.console.client.actions.sys;

import com.alibaba.fastjson2.JSONObject;
import com.uh.console.client.base.BaseActions;
import com.uh.console.client.base.ConsoleWebClient;

public class DictActions extends BaseActions {

    private static final String DICT_BASE = ConsoleWebClient.WEB_API_PREFIX + "/system/dict/data";

    public DictActions(ConsoleWebClient client) {
        super(client);
    }

    public JSONObject dictType(String dictType) {
        return getJson(DICT_BASE + "/type/" + dictType, null);
    }
}

