package com.upc.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SwaggerStartupLogger {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String baseUrl = "http://localhost:" + serverPort + contextPath;
        String message = "\n" +
                "================================================================================\n" +
                "  🚀 APLICACIÓN INICIADA CON ÉXITO\n" +
                "================================================================================\n" +
                "  Swagger UI interactivo:\n" +
                "  👉 " + baseUrl + "/swagger-ui.html (o " + baseUrl + "/swagger-ui/index.html)\n\n" +
                "  Especificación OpenAPI en JSON:\n" +
                "  👉 " + baseUrl + "/v3/api-docs\n" +
                "================================================================================\n";

        System.out.println(message);
    }
}