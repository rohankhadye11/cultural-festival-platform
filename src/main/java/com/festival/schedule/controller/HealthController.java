package com.festival.schedule.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check and system verification controller.
 * Enables DevOps smoke testing and staging verification.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:cultural-festival-platform}")
    private String applicationName;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("port", serverPort);
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("container", "Embedded Apache Tomcat 10.x");
        health.put("databaseTarget", "PostgreSQL 15+ (festival_db)");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> getWelcome() {
        Map<String, Object> welcome = new LinkedHashMap<>();
        welcome.put("message", "Welcome to the Automated Cultural Festival Schedule Platform (FestPulse) API");
        welcome.put("version", "1.0.0-RELEASE (Phase 1 MVP)");
        welcome.put("documentation", "docs/03_Architecture_SRS.md");
        welcome.put("endpoints", Map.of(
            "health", "/api/v1/health",
            "events", "/api/v1/events",
            "venues", "/api/v1/venues",
            "auth", "/api/v1/auth/login"
        ));
        return ResponseEntity.ok(welcome);
    }
}
