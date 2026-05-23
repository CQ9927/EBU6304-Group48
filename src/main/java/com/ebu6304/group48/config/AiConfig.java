package com.ebu6304.group48.config;

/**
 * Reads the DeepSeek API key from environment, system property, or built-in default.
 * Priority: env var > system property > hardcoded default.
 */
public final class AiConfig {

    private static final String ENV_KEY = "DEEPSEEK_API_KEY";
    private static final String PROP_KEY = "deepseek.api.key";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";
    private static final String DEFAULT_KEY = "sk-6c7b00ef2e054781984289c3c3f7052d";

    private static volatile String cachedKey;
    private static volatile boolean cached;

    private AiConfig() {
    }

    public static String getApiKey() {
        if (!cached) {
            String key = System.getenv(ENV_KEY);
            if (key == null || key.isBlank()) {
                key = System.getProperty(PROP_KEY);
            }
            if (key == null || key.isBlank()) {
                key = DEFAULT_KEY;
            }
            cachedKey = key.trim();
            cached = true;
        }
        return cachedKey;
    }

    public static boolean isAvailable() {
        return getApiKey() != null;
    }

    public static String getApiUrl() {
        return API_URL;
    }

    public static String getModel() {
        return MODEL;
    }
}
