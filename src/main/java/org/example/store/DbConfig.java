package org.example.store;

public final class DbConfig {
    private DbConfig() {
    }

    public static String url() {
        return envOrDefault(
                "DB_URL",
                "jdbc:mysql://localhost:3306/mini_siga?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
    }

    public static String user() {
        return envOrDefault("DB_USER", "root");
    }

    public static String password() {
        return envOrDefault("DB_PASSWORD", "");
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
