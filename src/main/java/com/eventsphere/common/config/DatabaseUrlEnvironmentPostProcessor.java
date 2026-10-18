package com.eventsphere.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalizes cloud database URLs (such as Aiven, Render, Railway, Heroku) into standard JDBC URLs.
 * Handles cases where DB_URL or DATABASE_URL begins with "mysql://" and contains embedded credentials.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUrlEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = environment.getProperty("DB_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = environment.getProperty("DATABASE_URL");
        }
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = environment.getProperty("spring.datasource.url");
        }

        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        rawUrl = rawUrl.trim();

        String jdbcUrl;
        String extractedUser = null;
        String extractedPass = null;

        if (rawUrl.startsWith("mysql://")) {
            String rest = rawUrl.substring("mysql://".length());
            if (rest.contains("@")) {
                int atIndex = rest.indexOf('@');
                String userPass = rest.substring(0, atIndex);
                String hostAndRest = rest.substring(atIndex + 1);

                int colonIndex = userPass.indexOf(':');
                if (colonIndex != -1) {
                    extractedUser = userPass.substring(0, colonIndex);
                    extractedPass = userPass.substring(colonIndex + 1);
                } else {
                    extractedUser = userPass;
                }
                jdbcUrl = "jdbc:mysql://" + hostAndRest;
            } else {
                jdbcUrl = "jdbc:mysql://" + rest;
            }
        } else if (rawUrl.startsWith("jdbc:mysql://")) {
            jdbcUrl = rawUrl;
            // Check if credentials are accidentally embedded in jdbc:mysql://user:pass@host:port/db
            String rest = jdbcUrl.substring("jdbc:mysql://".length());
            if (rest.contains("@")) {
                int atIndex = rest.indexOf('@');
                String userPass = rest.substring(0, atIndex);
                String hostAndRest = rest.substring(atIndex + 1);
                int colonIndex = userPass.indexOf(':');
                if (colonIndex != -1) {
                    extractedUser = userPass.substring(0, colonIndex);
                    extractedPass = userPass.substring(colonIndex + 1);
                } else {
                    extractedUser = userPass;
                }
                jdbcUrl = "jdbc:mysql://" + hostAndRest;
            }
        } else if (!rawUrl.startsWith("jdbc:")) {
            // Missing jdbc: prefix entirely
            if (rawUrl.contains("@")) {
                int atIndex = rawUrl.indexOf('@');
                String userPass = rawUrl.substring(0, atIndex);
                String hostAndRest = rawUrl.substring(atIndex + 1);
                int colonIndex = userPass.indexOf(':');
                if (colonIndex != -1) {
                    extractedUser = userPass.substring(0, colonIndex);
                    extractedPass = userPass.substring(colonIndex + 1);
                } else {
                    extractedUser = userPass;
                }
                jdbcUrl = "jdbc:mysql://" + hostAndRest;
            } else {
                jdbcUrl = "jdbc:mysql://" + rawUrl;
            }
        } else {
            jdbcUrl = rawUrl;
        }

        Map<String, Object> normalized = new HashMap<>();
        normalized.put("spring.datasource.url", jdbcUrl);
        normalized.put("DB_URL", jdbcUrl);

        if (extractedUser != null && !extractedUser.isBlank()) {
            normalized.put("spring.datasource.username", extractedUser);
            normalized.put("DB_USERNAME", extractedUser);
        }
        if (extractedPass != null && !extractedPass.isBlank()) {
            normalized.put("spring.datasource.password", extractedPass);
            normalized.put("DB_PASSWORD", extractedPass);
        }

        environment.getPropertySources().addFirst(new MapPropertySource("normalizedDatabaseProps", normalized));
        log.info("Database URL normalized to JDBC format: {}", maskJdbcUrl(jdbcUrl));
    }

    private String maskJdbcUrl(String url) {
        if (url == null) return null;
        return url.replaceAll(":[^/@:]+@", ":****@");
    }
}
