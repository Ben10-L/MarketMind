package com.marketmind.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Public settings for key-free API integrations. Override via environment variables. */
public final class ApiConfig {
    private final Properties settings = new Properties();

    public ApiConfig() {
        try (InputStream stream = getClass().getResourceAsStream("/config.properties")) {
            if (stream != null) settings.load(stream);
        } catch (IOException e) {
            System.err.println("Could not load config.properties: " + e.getMessage());
        }
    }

    public boolean useWorldBank() {
        return Boolean.parseBoolean(settings.getProperty("worldbank.enabled", "true"));
    }

    public String ollamaUrl() {
        return value("OLLAMA_URL", "ollama.url", "http://localhost:11434");
    }

    public String ollamaModel() {
        return value("OLLAMA_MODEL", "ollama.model", "qwen3:4b");
    }

    public String searxngUrl() {
        return value("SEARXNG_URL", "searxng.url", "http://localhost:8888");
    }

    public int searchLimit() {
        try {
            return Math.max(1, Math.min(10,
                    Integer.parseInt(settings.getProperty("search.max-results", "5"))));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private String value(String env, String property, String defaultValue) {
        String fromEnv = System.getenv(env);
        return (fromEnv != null && !fromEnv.isBlank() ? fromEnv :
                settings.getProperty(property, defaultValue)).trim();
    }
}
