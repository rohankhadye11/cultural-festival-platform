# 06. Selenium Automated Test Plan & Defect Lifecycle

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Test Strategy, Automation Specification & Defect Lifecycle Guide  
**Phase:** Phase 3 - Continuous Integration & Automated Testing  
**Audience:** QA Automation Engineers, DevOps Engineers, and Evaluators  
**Document Version:** 3.0.0-RELEASE  
**Date:** October 2026  

---

## 1. Test Strategy & Scope

The automated test strategy combines unit testing, integration slice testing, and end-to-end browser automation using **Selenium WebDriver** and **WebDriverManager**.

```
+-------------------------------------------------------------------------------------------------+
|                                FESTPULSE TEST AUTOMATION PYRAMID                                |
+-------------------------------------------------------------------------------------------------+
|                                                                                                 |
|                                     / \                                                         |
|                                    /   \                                                        |
|                                   / UI  \        [Selenium Headless E2E Tests]                  |
|                                  / Tests \       - 3 User Journeys                              |
|                                 /---------\      - Automated Failure Screenshots                |
|                                /           \                                                    |
|                               / Integration \    [Spring Boot MockMvc & JPA Slices]             |
|                              /     Tests     \   - REST API Controllers & Error Responses       |
|                             /-----------------\                                                 |
|                            /                   \                                                |
|                           /     Unit Tests      \  [JUnit 5 & Mockito Tests]                    |
|                          /                       \ - Conflict Engine Overlap Math               |
|                         +-------------------------+ - Venue & Event Business Services          |
+-------------------------------------------------------------------------------------------------+
```

### Key Automation Principles:
- **Zero Manual Driver Setup:** Utilizes `WebDriverManager` to dynamically detect, download, and configure matching browser binaries (ChromeDriver).
- **Headless Pipeline Execution:** Runs in headless mode (`--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`) to ensure compatibility with headless CI servers, Docker containers, and virtual machines.
- **Dynamic Port Isolation:** Tests run against Spring Boot's `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`, eliminating port collision during parallel test runs.

---

## 2. Specification of the 3 Critical User Journeys

All user journeys are implemented in [`src/test/java/com/festival/schedule/ui/SeleniumUITest.java`](../src/test/java/com/festival/schedule/ui/SeleniumUITest.java):

### Journey 1: Landing Page & Branding Verification (`testLandingPageAndTitle`)
- **Objective:** Verify that the embedded Tomcat container serves the primary web dashboard, loads all CSS/DOM tokens, and displays expected institutional branding.
- **Execution Steps:**
  1. Browser executes HTTP GET request to `http://localhost:<PORT>/`.
  2. Inspects `driver.getTitle()`.
  3. Locates DOM element `<h1>` via `By.tagName("h1")`.
- **Pass Assertions:**
  - Page title is not null and contains `"FestPulse"` or `"Automated Cultural Festival"`.
  - Primary heading contains `"FestPulse"`.

### Journey 2: Direct REST API Availability Check (`testVenueApiEndpointInBrowser`)
- **Objective:** Verify that the Spring Web MVC `DispatcherServlet` routes and JPA persistence layers are active and serving JSON responses to web clients.
- **Execution Steps:**
  1. Browser navigates to `http://localhost:<PORT>/api/v1/venues`.
  2. Retrieves raw page body via `driver.getPageSource()`.
  3. Navigates to `http://localhost:<PORT>/api/v1/health`.
- **Pass Assertions:**
  - Body contains a valid JSON array structure `[` and `]`.
  - Health check endpoint returns status `"UP"`.

### Journey 3: Error Page & 404 Routing Verification (`testNonExistentPageReturns404`)
- **Objective:** Verify that unmapped routes or non-existent entity IDs are captured by the `GlobalExceptionHandler` and return a standardized HTTP 404 payload rather than leaking Tomcat stack traces.
- **Execution Steps:**
  1. Browser attempts navigation to `http://localhost:<PORT>/api/v1/venues/99999`.
  2. Reads rendered error response text.
- **Pass Assertions:**
  - Response body contains HTTP status `"404"` or `"Not Found"`.
  - Demonstrates secure error masking in compliance with non-functional security requirements.

---

## 3. Automated Screenshot Mechanism on Test Failure

To eliminate guesswork during remote CI build failures, `SeleniumUITest.java` includes an automated screenshot capture mechanism using JUnit 5's `TestWatcher` API.

### How It Works:
```
                               +-----------------------------+
                               |     Test Execution Fails    |
                               +-----------------------------+
                                              |
                                              v
                              +-------------------------------+
                              | JUnit 5 TestWatcher Triggered |
                              |      testFailed() hook        |
                              +-------------------------------+
                                              |
                                              v
                              +-------------------------------+
                              | TakesScreenshot.getScreenshot |
                              | Output: Temporary PNG File    |
                              +-------------------------------+
                                              |
                                              v
                              +-------------------------------+
                              | Save to target/screenshots/   |
                              | FAILED_<method>_<time>.png    |
                              +-------------------------------+
                                              |
                                              v
                              +-------------------------------+
                              | Jenkins Post-Build Action:    |
                              | archiveArtifacts *.png        |
                              +-------------------------------+
```

1. **Failure Interception:** The `@RegisterExtension` creates a `TestWatcher` that intercepts test exceptions before the browser driver quits.
2. **Snapshot Capture:** The active `WebDriver` instance is cast to `TakesScreenshot`, capturing the full viewport at the exact moment of failure.
3. **Artifact Archiving:** The screenshot is saved to `target/screenshots/` with the filename pattern:
   ```
   FAILED_<TestMethodName>_<YYYYMMDD_HHMMSS>.png
   ```
4. **Jenkins Pipeline Integration:** The `Jenkinsfile` contains a post-build step that automatically publishes and preserves these screenshots:
   ```groovy
   archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/screenshots/*.png'
   ```

---

## 4. Task 10: Intentional Defect Injection & Resolution Cycle

In software quality assurance and DevOps demonstrations, introducing a deliberate defect proves that automated quality gates actively block broken builds from progressing to staging.

Follow this two-part procedure:

### Part A: Injecting the Defect (Breaking the Pipeline)

1. Open [`src/test/java/com/festival/schedule/ui/SeleniumUITest.java`](../src/test/java/com/festival/schedule/ui/SeleniumUITest.java).
2. Locate the `testLandingPageAndTitle()` method (around line 113).
3. Intentionally alter the assertion to expect a non-existent title string:
   ```java
   // INTENTIONAL DEFECT INJECTION FOR TASK 10:
   assertTrue(
       pageTitle.contains("INVALID_TITLE_DEFECT_TEST"),
       "This assertion is intentionally broken to demonstrate Jenkins pipeline failure"
   );
   ```
4. Commit and push the defect:
   ```powershell
   git add src/test/java/com/festival/schedule/ui/SeleniumUITest.java
   git commit -m "test(defect): intentionally inject failing assertion in UI test suite"
   git push origin main
   ```
5. **Observe Jenkins Pipeline Failure:**
   - Jenkins triggers automatically (via webhook or poll).
   - **Stage 1 (Checkout):** GREEN &check;
   - **Stage 2 (Build):** GREEN &check;
   - **Stage 3 (Unit & UI Tests):** **RED &cross; (FAILED)**
   - **Stage 4 (Package) & Stage 5 (Deploy):** **SKIPPED (Blocked)**
6. **Inspect Failure Diagnostics in Jenkins:**
   - Navigate to the failed build in Jenkins.
   - Click **"Test Result"** &rarr; inspect `testLandingPageAndTitle` failure stack trace.
   - Click **"Artifacts"** &rarr; open the generated screenshot:  
     `target/screenshots/FAILED_testLandingPageAndTitle_*.png`  
     Notice that the screenshot confirms the page actually loaded, confirming the issue is an assertion discrepancy!

---

### Part B: Resolving the Defect (Restoring the Green Pipeline)

1. Return to [`src/test/java/com/festival/schedule/ui/SeleniumUITest.java`](../src/test/java/com/festival/schedule/ui/SeleniumUITest.java).
2. Restore the original valid assertion:
   ```java
   assertTrue(
       pageTitle.contains("FestPulse") || pageTitle.contains("Automated Cultural Festival"),
       "Page title should contain 'FestPulse' or 'Automated Cultural Festival'. Actual title: " + pageTitle
   );
   ```
3. Commit and push the fix:
   ```powershell
   git add src/test/java/com/festival/schedule/ui/SeleniumUITest.java
   git commit -m "fix(test): resolve landing page title assertion defect for Task 10"
   git push origin main
   ```
4. **Observe Jenkins Pipeline Recovery:**
   - All 5 stages turn **GREEN &check;**:
     - `Checkout` &rarr; `Build` &rarr; `Unit & UI Tests` &rarr; `Package` &rarr; `Deploy (Simulated)`
   - The build artifact (`cultural-festival-platform-0.0.1-SNAPSHOT.jar`) is archived and verified for deployment.
