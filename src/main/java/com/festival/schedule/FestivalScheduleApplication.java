package com.festival.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * Main entry point for the Automated Cultural Festival Schedule Platform (FestPulse).
 * 
 * Tech Stack:
 * - Java 21 LTS
 * - Spring Boot 3.3.x
 * - Embedded Apache Tomcat 10.x
 * - Spring Data JPA / Hibernate
 * - PostgreSQL 15+
 */
@SpringBootApplication
public class FestivalScheduleApplication {

    private static final Logger logger = LoggerFactory.getLogger(FestivalScheduleApplication.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(FestivalScheduleApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String appName = env.getProperty("spring.application.name", "cultural-festival-platform");

        logger.info("====================================================================");
        logger.info(" Application '{}' is running!", appName);
        logger.info(" Local URL:        http://localhost:{}", port);
        logger.info(" Health Endpoint:  http://localhost:{}/api/v1/health", port);
        logger.info(" Welcome Info:     http://localhost:{}/api/v1/welcome", port);
        logger.info("====================================================================");
    }
}
