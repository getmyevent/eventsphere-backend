package com.eventsphere.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();
    private final ConfigurableEnvironment environment = new StandardEnvironment();
    private final SpringApplication application = new SpringApplication();

    @Test
    @DisplayName("Converts cloud mysql:// URI with credentials into jdbc:mysql:// format and extracts credentials")
    void shouldConvertCloudMysqlUriWithCredentials() {
        System.setProperty("DB_URL", "mysql://avnadmin:AVNS_secret123@mysql-host.aivencloud.com:12968/defaultdb?ssl-mode=REQUIRED");
        try {
            processor.postProcessEnvironment(environment, application);

            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:mysql://mysql-host.aivencloud.com:12968/defaultdb?ssl-mode=REQUIRED");
            assertThat(environment.getProperty("spring.datasource.username"))
                    .isEqualTo("avnadmin");
            assertThat(environment.getProperty("spring.datasource.password"))
                    .isEqualTo("AVNS_secret123");
        } finally {
            System.clearProperty("DB_URL");
        }
    }

    @Test
    @DisplayName("Preserves already well-formed jdbc:mysql:// URI without changes")
    void shouldPreserveJdbcMysqlUri() {
        System.setProperty("DB_URL", "jdbc:mysql://localhost:3306/eventsphere_dev?ssl-mode=REQUIRED");
        try {
            processor.postProcessEnvironment(environment, application);

            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:mysql://localhost:3306/eventsphere_dev?ssl-mode=REQUIRED");
        } finally {
            System.clearProperty("DB_URL");
        }
    }

    @Test
    @DisplayName("Handles bare host and path without jdbc: scheme prefix")
    void shouldPrefixBareHostWithJdbcMysql() {
        System.setProperty("DB_URL", "mysql-host.aivencloud.com:12968/defaultdb?ssl-mode=REQUIRED");
        try {
            processor.postProcessEnvironment(environment, application);

            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:mysql://mysql-host.aivencloud.com:12968/defaultdb?ssl-mode=REQUIRED");
        } finally {
            System.clearProperty("DB_URL");
        }
    }
}
