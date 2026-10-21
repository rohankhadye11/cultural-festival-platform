# 05. CI/CD Pipeline & Jenkins Automation Guide

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Continuous Integration & Delivery Guide (CI/CD)  
**Phase:** Phase 3 - Continuous Integration & Automated Testing  
**Audience:** DevOps Engineers, QA Automation Teams, and Academic Evaluators  
**Document Version:** 3.0.0-RELEASE  
**Date:** October 2026  

---

## 1. CI/CD Architecture Overview

The Continuous Integration & Continuous Delivery (CI/CD) pipeline automates code quality verification, compilation, headless browser execution, packaging, and deployment simulation every time code is committed to GitHub.

```
+-------------------------------------------------------------------------------------------------+
|                                FESTPULSE CI/CD PIPELINE FLOW                                    |
+-------------------------------------------------------------------------------------------------+
|                                                                                                 |
|   +---------------+        +----------------+        +--------------------------------------+   |
|   |  Developer    | ---->  | GitHub Repo    | ---->  | Jenkins Controller (Port 9090)       |   |
|   |  git push     |        | Webhook/Poll   |        | Pipeline as Code (Jenkinsfile)       |   |
|   +---------------+        +----------------+        +--------------------------------------+   |
|                                                                  |                              |
|           +------------------------------------------------------+                              |
|           |                                                                                     |
|           v                                                                                     |
|   [Stage 1: Checkout] --------> Pulls latest commit from GitHub SCM                             |
|           |                                                                                     |
|           v                                                                                     |
|   [Stage 2: Build] -----------> `mvn clean compile` (Java 21 bytecode verification)             |
|           |                                                                                     |
|           v                                                                                     |
|   [Stage 3: Tests] -----------> `mvn test` (JUnit 5 + Headless Selenium UI Suite)               |
|                                 - Publishes XML test reports to Jenkins UI                      |
|                                 - Archives failure screenshots (if assertions fail)             |
|           |                                                                                     |
|           v                                                                                     |
|   [Stage 4: Package] ---------> `mvn package -DskipTests` (Produces executable Spring Boot JAR) |
|                                 - Fingerprints & archives `target/*.jar`                        |
|           |                                                                                     |
|           v                                                                                     |
|   [Stage 5: Deploy (Sim)] ----> Background startup via `nohup` / daemon on target DEPLOY_ENV   |
|                                 - Performs automated health probe verification                  |
+-------------------------------------------------------------------------------------------------+
```

---

## 2. Step 1: Installing & Running Jenkins

> [!WARNING]
> **Port Conflict Notice:**  
> The Spring Boot application runs on port `8080`. By default, Jenkins also attempts to bind to port `8080`. To avoid port collision, **always run Jenkins on port `9090`**.

Choose one of the three installation methods below:

### Option A: Running Jenkins via Docker (Recommended)
```bash
docker run -d \
  --name festpulse-jenkins \
  -p 9090:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts-jdk17
```

### Option B: Running Jenkins Standalone WAR (Windows / Mac / Linux)
1. Download `jenkins.war` from the official Jenkins site: [https://www.jenkins.io/download/](https://www.jenkins.io/download/).
2. Open PowerShell and run:
   ```powershell
   java -jar jenkins.war --httpPort=9090
   ```

### Option C: Windows Native MSI Installer
1. Install Jenkins using the Windows installer.
2. During setup, configure the port to `9090`.

---

## 3. Step 2: Initial Jenkins Setup & Plugin Configuration

1. **Unlock Jenkins:**
   - Open your browser to: [http://localhost:9090/](http://localhost:9090/)
   - Copy the initial administrator password from the path shown on the screen (or from your terminal logs):
     - *Windows WAR:* `C:\Users\<Username>\.jenkins\secrets\initialAdminPassword`
     - *Docker:* `docker exec festpulse-jenkins cat /var/jenkins_home/secrets/initialAdminPassword`
2. **Install Suggested Plugins:**
   - Select **"Install suggested plugins"** (installs Git, Pipeline, JUnit, Workspace Cleanup, Timestamper).
3. **Configure JDK 21 & Maven in Jenkins:**
   - Go to **Manage Jenkins** &rarr; **Tools**.
   - Under **JDK Installations**:
     - Name: `JDK_21`
     - Path: Path to your JDK 21 directory (e.g., `C:\Program Files\Java\jdk-21`).
   - Under **Maven Installations**:
     - Name: `Maven_3.9`
     - Select **"Install automatically"** or provide your local Maven directory.

---

## 4. Step 3: Create & Configure the Pipeline Job

1. From the Jenkins Dashboard, click **"New Item"**.
2. Enter Item Name: `cultural-festival-platform-pipeline`.
3. Select **"Pipeline"** and click **OK**.
4. Under the **General** tab:
   - Check **"This project is parameterized"**.
   - Click **Add Parameter** &rarr; **Choice Parameter**:
     - **Name:** `DEPLOY_ENV`
     - **Choices:**
       ```
       staging
       production
       ```
     - **Description:** `Target deployment environment for simulated deployment.`
   - Click **Add Parameter** &rarr; **Boolean Parameter**:
     - **Name:** `RUN_TESTS`
     - **Default Value:** `Checked` (true)
     - **Description:** `Execute automated unit and Selenium UI test suites.`
5. Under the **Build Triggers** tab:
   - Check **"GitHub hook trigger for GITScm polling"** (for automated webhook triggering).
   - Alternatively, check **"Poll SCM"** and enter Schedule:
     ```
     H/5 * * * *
     ```
     *(Polls GitHub repository for new commits every 5 minutes).*
6. Under the **Pipeline** tab:
   - **Definition:** `Pipeline script from SCM`
   - **SCM:** `Git`
   - **Repository URL:** `https://github.com/rohankhadye11/cultural-festival-platform.git`
   - **Credentials:** None required for public repository (or add your GitHub Personal Access Token if private).
   - **Branch Specifier:** `*/main` (or `*/feature/*` for PR validation).
   - **Script Path:** `Jenkinsfile`
7. Click **Save**.

---

## 5. Step 4: Jenkinsfile Stage Mapping to Project Deliverables

| Pipeline Stage | Underlying Command | Purpose & Academic Task Mapping |
| :--- | :--- | :--- |
| **1. Checkout** | `checkout scm` | Pulls the active repository branch and commit metadata into the Jenkins workspace. |
| **2. Build** | `mvn clean compile` | **Task 7:** Verifies Java 21 compilation, resolves dependencies, and detects syntax errors early. |
| **3. Unit & UI Tests**| `mvn test` | **Task 8 & 10:** Executes JUnit 5 unit tests, mock validation, and headless Selenium browser journeys. Publishes JUnit XML reports and archives failure screenshots. |
| **4. Package** | `mvn package -DskipTests` | **Task 9:** Assembles the standalone executable Spring Boot fat JAR (`cultural-festival-platform-0.0.1-SNAPSHOT.jar`) and records fingerprint. |
| **5. Deploy (Simulated)**| Background `java -jar` | **Task 11:** Starts the Spring Boot daemon in the selected environment (`staging` or `production`) and verifies `/api/v1/health`. |

---

## 6. Step 5: Executing & Monitoring Builds

### Manual Execution with Parameters:
1. Open the job in Jenkins: [http://localhost:9090/job/cultural-festival-platform-pipeline/](http://localhost:9090/job/cultural-festival-platform-pipeline/)
2. Click **"Build with Parameters"** in the left sidebar.
3. Select `DEPLOY_ENV` = `staging`, keep `RUN_TESTS` = `true`.
4. Click **"Build"**.

### Viewing Diagnostic Outputs:
- **Pipeline Stage View:** Visualizes duration and status (Green = Passed, Red = Failed) for each stage.
- **Console Output:** Click on the active build number (e.g. `#1`) &rarr; **"Console Output"** to stream Maven and Spring Boot logs in real time.
- **Test Results Trend:** Click **"Test Result"** to inspect passed/failed JUnit test counts and execution times.
- **Artifacts:** Click on the build summary to download the generated JAR or inspect any failure screenshots archived in `target/screenshots/`.
