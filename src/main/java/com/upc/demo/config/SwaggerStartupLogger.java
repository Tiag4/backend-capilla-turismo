package com.upc.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SwaggerStartupLogger {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String baseUrl = "http://localhost:" + serverPort + (contextPath != null ? contextPath : "");
        String message = "\n" +
                "================================================================================\n" +
                "  \uD83D\uDE80 APLICACI\u00D3N INICIADA CON \u00C9XITO\n" +
                "================================================================================\n" +
                "  Swagger UI interactivo:\n" +
                "  \uD83D\uDC49 " + baseUrl + "/swagger-ui.html (o " + baseUrl + "/swagger-ui/index.html)\n\n" +
                "  Especificaci\u00F3n OpenAPI en JSON:\n" +
                "  \uD83D\uDC49 " + baseUrl + "/v3/api-docs\n" +
                "================================================================================\n";

        try {
            PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
            out.println(message);
        } catch (Exception e) {
            System.out.println(message);
        }
    }
}