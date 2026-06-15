package com.example.devforge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class NeonDatabaseEnvironmentPostProcessorTests {

    private final NeonDatabaseEnvironmentPostProcessor processor =
            new NeonDatabaseEnvironmentPostProcessor();

    @Test
    void preservesJdbcPostgresUrlAndNormalizesChannelBinding() {
        StandardEnvironment environment = environmentWith(Map.of(
                "DB_URL",
                "jdbc:postgresql://db.example.com/app?sslmode=require&channel_binding=require"));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com/app?sslmode=require&channelBinding=require");
    }

    @Test
    void convertsNeonPostgresUrlToJdbcUrl() {
        StandardEnvironment environment = environmentWith(Map.of(
                "DATABASE_URL",
                "postgresql://user:password@db.example.com/app?sslmode=require"));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com/app?sslmode=require");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("password");
    }

    @Test
    void explicitDbUrlTakesPriorityOverDatabaseUrl() {
        StandardEnvironment environment = environmentWith(Map.of(
                "DB_URL", "jdbc:postgresql://render.example.com/app?sslmode=require",
                "DATABASE_URL", "postgresql://neon.example.com/other?sslmode=require"));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://render.example.com/app?sslmode=require");
    }

    private StandardEnvironment environmentWith(Map<String, Object> properties) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        return environment;
    }
} 