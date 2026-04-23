package com.uh.console.client.config;

import com.uh.console.client.domain.ConsoleClientConf;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigManager {

    private static final String DEFAULT_CONFIG_FILE = "console-config.yml";

    private ConfigManager() {
    }

    public static ConsoleClientConf getConsoleConfig() {
        String configFile = System.getProperty("console.config.file", DEFAULT_CONFIG_FILE);

        try (InputStream inputStream = openConfig(configFile)) {
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(new Constructor(ConsoleRoot.class, options));
            ConsoleRoot root = yaml.load(inputStream);
            if (root == null || root.getConsole() == null) {
                throw new IllegalStateException("console config is empty: " + configFile);
            }
            return root.getConsole();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load console config: " + configFile, e);
        }
    }

    private static InputStream openConfig(String configFile) throws IOException {
        InputStream classpathStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
        if (classpathStream != null) {
            return classpathStream;
        }

        Path path = Paths.get(configFile);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }

        throw new IOException("Config file not found in classpath or filesystem: " + configFile);
    }

    public static class ConsoleRoot {
        private ConsoleClientConf console;

        public ConsoleClientConf getConsole() {
            return console;
        }

        public void setConsole(ConsoleClientConf console) {
            this.console = console;
        }
    }
}

