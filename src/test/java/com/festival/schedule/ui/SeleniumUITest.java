package com.festival.schedule.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End UI and Browser Automation Test Suite using Selenium WebDriver and WebDriverManager.
 * 
 * Tests run against an isolated Spring Boot embedded Tomcat container on a random port.
 * Configured for Headless execution in CI/CD pipelines (Jenkins / GitHub Actions).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:uitestdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class SeleniumUITest {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumUITest.class);
    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");

    @LocalServerPort
    private int port;

    private static WebDriver driver;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        // Automatically download and configure the matching ChromeDriver binary
        WebDriverManager.chromedriver().setup();

        try {
            Files.createDirectories(SCREENSHOT_DIR);
        } catch (IOException e) {
            logger.error("Failed to create screenshot directory: {}", e.getMessage());
        }
    }

    @BeforeEach
    void setupTest() {
        this.baseUrl = "http://localhost:" + port;

        ChromeOptions options = new ChromeOptions();
        // Mandatory options for headless container execution (Jenkins / Docker / Linux agents)
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
    }

    /**
     * Test Watcher extension to automatically capture and archive a screenshot upon test failure.
     */
    @RegisterExtension
    final TestWatcher watcher = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            String testMethodName = context.getRequiredTestMethod().getName();
            logger.warn("Test '{}' FAILED. Capturing automated diagnostic screenshot...", testMethodName);
            captureScreenshot(testMethodName);
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            logger.info("Test '{}' PASSED successfully.", context.getRequiredTestMethod().getName());
        }
    };

    @AfterEach
    void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.warn("Error quitting WebDriver instance: {}", e.getMessage());
            }
        }
    }

    /**
     * User Journey 1:
     * Load the festival platform landing page/dashboard and verify application branding and title.
     */
    @Test
    @DisplayName("Journey 1: Verify landing page renders and displays expected branding title")
    void testLandingPageAndTitle() {
        driver.get(baseUrl + "/");

        String pageTitle = driver.getTitle();
        logger.info("Loaded landing page. Page title: '{}'", pageTitle);

        assertNotNull(pageTitle, "Page title must not be null");
        assertTrue(
                pageTitle.contains("FestPulse") || pageTitle.contains("Automated Cultural Festival"),
                "Page title should contain 'FestPulse' or 'Automated Cultural Festival'. Actual title: " + pageTitle
        );

        // Verify the primary hero header is present in the DOM
        WebElement heading = driver.findElement(By.tagName("h1"));
        assertNotNull(heading, "Landing page should contain an <h1> heading");
        assertTrue(heading.getText().contains("FestPulse"), "Heading should contain 'FestPulse'");
    }

    /**
     * User Journey 2:
     * Navigate to the /api/v1/venues endpoint via the browser and verify the API is up and serving responses.
     */
    @Test
    @DisplayName("Journey 2: Query Venue API directly in browser to verify REST endpoint availability")
    void testVenueApiEndpointInBrowser() {
        driver.get(baseUrl + "/api/v1/venues");

        String pageSource = driver.getPageSource();
        logger.info("Navigated to /api/v1/venues. Page content length: {} chars", pageSource.length());

        // In a browser, JSON is rendered inside <body> or <pre> tags
        assertNotNull(pageSource, "API response body must not be null");
        assertTrue(
                pageSource.contains("[") || pageSource.contains("]"),
                "API endpoint /api/v1/venues must return a JSON array structure"
        );

        // Also verify the /api/v1/health endpoint
        driver.get(baseUrl + "/api/v1/health");
        String healthSource = driver.getPageSource();
        assertTrue(
                healthSource.contains("\"status\"") && healthSource.contains("\"UP\""),
                "Health endpoint must return status UP"
        );
    }

    /**
     * User Journey 3:
     * Attempt to navigate to a non-existent route and verify a 404 response or error page is displayed.
     */
    @Test
    @DisplayName("Journey 3: Verify 404 Not Found error handling on non-existent endpoint")
    void testNonExistentPageReturns404() {
        driver.get(baseUrl + "/api/v1/venues/99999");

        String responseBody = driver.getPageSource();
        logger.info("Requested non-existent venue ID. Response body preview: {}", 
                responseBody.substring(0, Math.min(responseBody.length(), 200)));

        // Verify that our GlobalExceptionHandler returned an HTTP 404 error payload
        assertTrue(
                responseBody.contains("404") || responseBody.contains("Not Found"),
                "Accessing non-existent venue ID must yield a 404 Not Found response. Actual body: " + responseBody
        );
    }

    /**
     * Utility method to capture and save a PNG screenshot using TakesScreenshot.
     *
     * @param testName Name of the failed test method
     */
    private static void captureScreenshot(String testName) {
        if (driver instanceof TakesScreenshot takesScreenshot) {
            try {
                File srcFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("FAILED_%s_%s.png", testName, timestamp);
                Path destPath = SCREENSHOT_DIR.resolve(fileName);

                Files.copy(srcFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Screenshot successfully archived to: {}", destPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Failed to save screenshot for test '{}': {}", testName, e.getMessage());
            } catch (WebDriverException e) {
                logger.warn("WebDriver unable to take screenshot: {}", e.getMessage());
            }
        }
    }
}
