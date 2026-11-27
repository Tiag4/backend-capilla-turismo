package com.upc.demo.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically loads .env file properties into the Spring Environment
 * with highest priority before application context initialization.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = new File(".env");
        if (!envFile.exists() || !envFile.isFile()) {
            envFile = new File("../.env");
        }
        if (!envFile.exists() || !envFile.isFile()) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envFile.toPath(), StandardCharsets.UTF_8);
            Map<String, Object> props = new HashMap<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int separatorIndex = trimmed.indexOf('=');
                String key = trimmed.substring(0, separatorIndex).trim();
                String value = trimmed.substring(separatorIndex + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    if (value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                props.put(key, value);
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
            if (!props.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource("dotEnvProperties", props));
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read .env file: " + e.getMessage());
        }
    }
}
