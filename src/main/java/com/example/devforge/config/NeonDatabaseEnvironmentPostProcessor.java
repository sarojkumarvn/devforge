package com.example.devforge.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class NeonDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "neonDatabaseEnvironment";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new LinkedHashMap<>();

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (StringUtils.hasText(databaseUrl)) {
            applyDatabaseUrl(databaseUrl, properties);
        } else if (!StringUtils.hasText(environment.getProperty("DB_URL"))) {
            applyDatabaseParts(environment, properties);
        }

        String dbUser = environment.getProperty("DB_USER");
        if (StringUtils.hasText(dbUser) && !StringUtils.hasText(environment.getProperty("DB_USERNAME"))) {
            properties.put("spring.datasource.username", dbUser);
        }

        String dbPassword = environment.getProperty("DB_PASSWORD");
        if (StringUtils.hasText(dbPassword)) {
            properties.putIfAbsent("spring.datasource.password", dbPassword);
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private void applyDatabaseUrl(String databaseUrl, Map<String, Object> properties) {
        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgresql".equalsIgnoreCase(scheme) && !"postgres".equalsIgnoreCase(scheme)) {
            return;
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());

        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }

        jdbcUrl.append(uri.getPath());

        Map<String, String> queryParams = parseQuery(uri.getRawQuery());
        if (!queryParams.containsKey("sslmode")) {
            queryParams.put("sslmode", "require");
        }
        if (queryParams.containsKey("channel_binding")) {
            queryParams.put("channelBinding", queryParams.remove("channel_binding"));
        }

        if (!queryParams.isEmpty()) {
            jdbcUrl.append('?');
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    jdbcUrl.append('&');
                }
                jdbcUrl.append(entry.getKey()).append('=').append(entry.getValue());
                first = false;
            }
        }

        properties.put("spring.datasource.url", jdbcUrl.toString());
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("spring.flyway.enabled", "true");
        properties.put("spring.jpa.hibernate.ddl-auto", "validate");

        String userInfo = uri.getRawUserInfo();
        if (StringUtils.hasText(userInfo)) {
            int separator = userInfo.indexOf(':');
            if (separator >= 0) {
                properties.put("spring.datasource.username", decode(userInfo.substring(0, separator)));
                properties.put("spring.datasource.password", decode(userInfo.substring(separator + 1)));
            } else {
                properties.put("spring.datasource.username", decode(userInfo));
            }
        }
    }

    private void applyDatabaseParts(ConfigurableEnvironment environment, Map<String, Object> properties) {
        String host = environment.getProperty("DB_HOST");
        String database = environment.getProperty("DB_NAME");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(database)) {
            return;
        }

        String port = environment.getProperty("DB_PORT", "5432");
        properties.put("spring.datasource.url", "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require");
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("spring.flyway.enabled", "true");
        properties.put("spring.jpa.hibernate.ddl-auto", "validate");
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (!StringUtils.hasText(rawQuery)) {
            return queryParams;
        }

        for (String pair : rawQuery.split("&")) {
            int separator = pair.indexOf('=');
            if (separator >= 0) {
                queryParams.put(decode(pair.substring(0, separator)), decode(pair.substring(separator + 1)));
            } else {
                queryParams.put(decode(pair), "");
            }
        }
        return queryParams;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
