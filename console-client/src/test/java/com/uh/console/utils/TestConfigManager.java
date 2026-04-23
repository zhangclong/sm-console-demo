package com.uh.console.utils;

import com.uh.console.client.config.ConfigManager;
import com.uh.console.client.domain.ConsoleClientConf;

public final class TestConfigManager {

    private TestConfigManager() {
    }

    public static ConsoleClientConf getConsoleConfig() {
        return ConfigManager.getConsoleConfig();
    }
}

