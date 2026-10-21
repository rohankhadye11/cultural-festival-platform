# 08. Continuous Delivery (CD) with Docker & Jenkins Guide

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Continuous Deployment (CD) Automation Guide  
**Phase:** Phase 4 - Containerization & Continuous Deployment  
**Academic Mapping:** Task 12 (Continuous Deployment to Docker via Jenkins)  
**Audience:** DevOps Engineers, Cloud Architects, and Evaluators  
**Document Version:** 4.0.0-RELEASE  
**Date:** October 2026  

---

## 1. Continuous Delivery (CD) Architecture Overview

Task 12 elevates the FestPulse CI pipeline into an automated **Continuous Delivery (CD)** pipeline. Every verified commit to the `main` branch is automatically packaged, converted into an immutable Docker image, pushed to the central Docker Hub registry, and deployed as a running container on port `8080`.

```
+-------------------------------------------------------------------------------------------------------------------+
|                                  FESTPULSE END-TO-END CD PIPELINE (TASK 12)                                       |
+-------------------------------------------------------------------------------------------------------------------+
|                                                                                                                   |
|   +---------------+      +-------------------+      +---------------------------------------------------------+   |
|   |  Developer    | ---> | GitHub Repository | ---> | Jenkins Controller (Port 9090)                          |   |
|   |  git push     |      | `main` branch     |      | Pipeline as Code (`Jenkinsfile`)                        |   |
|   +---------------+      +-------------------+      +---------------------------------------------------------+   |
|                                                                      |                                            |
|                  +---------------------------------------------------+                                            |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 1: Checkout] --------> Git SCM Clone                                                              |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 2: Build] -----------> `mvn clean compile` (Java 21 bytecode verification)                        |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 3: Tests] -----------> `mvn test` (Unit tests + Headless Selenium UI Suite)                       |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 4: Package] ---------> `mvn package -DskipTests` (Generates fat JAR)                              |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 5: Docker Build] ----> `docker build -t festpulse-app:latest -t festpulse-app:${BUILD_NUMBER} .`  |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 6: Docker Push] -----> Authenticates with `dockerhub-creds` and pushes image to Docker Hub        |
|                  |                                                                                                |
|                  v                                                                                                |
|          [Stage 7: Container Deploy] -> `docker rm -f festpulse-app || true`                                      |
|                                        `docker run -d --name festpulse-app -p 8080:8080 ...`                      |
|                                        Automated Health Check verification (`/api/v1/health`)                     |
+-------------------------------------------------------------------------------------------------------------------+
```

---

## 2. Updated 7-Stage Pipeline Reference

The updated [Jenkinsfile](file:///c:/Users/Rohan/Documents/DevOps%20Project/cultural-festival-platform/Jenkinsfile) defines seven distinct automated stages:

| Stage # | Stage Name | Technology | Operational Function |
| :--- | :--- | :--- | :--- |
| **1** | **Checkout** | Git SCM | Pulls the latest commit SHA from the target repository branch. |
| **2** | **Build** | Maven Wrapper | Verifies compilation and resolves all Spring Boot dependencies. |
| **3** | **Unit & UI Tests** | JUnit 5 & Selenium | Executes the test pyramid, publishes reports, and captures failure screenshots. |
| **4** | **Package** | Spring Boot Maven | Assembles the self-contained executable JAR (`cultural-festival-platform-0.0.1-SNAPSHOT.jar`). |
| **5** | **Build Docker Image** | Docker CLI | Compiles the [Dockerfile](file:///c:/Users/Rohan/Documents/DevOps%20Project/cultural-festival-platform/Dockerfile) into an Alpine-based runtime tagged with `latest` and `${env.BUILD_NUMBER}`. |
| **6** | **Push Docker Image** | Docker Hub & Credentials | Injects credentials via `withCredentials`, authenticates to Docker Hub via standard stdin, tags, and pushes image layers. |
| **7** | **Deploy Container** | Docker Engine | Automates container replacement: cleanly stops the old instance (`docker rm -f festpulse-app || true`) and starts the new image mapped to port `8080`. |

---

## 3. Configuring `dockerhub-creds` in Jenkins UI

To allow Jenkins to push container images to Docker Hub securely without hardcoding credentials in source code, configure a Jenkins credential with the ID **`dockerhub-creds`**.

### Step 3.1: Open Credentials Management

1. Open your browser and navigate to the Jenkins dashboard:  
   [http://localhost:9090/](http://localhost:9090/)
2. In the left-hand sidebar, click **Manage Jenkins**.
3. Under the **Security** section, click **Credentials**.
4. In the Credentials table, click **(global)** under the **Domains** column (or click **System** &rarr; **Global credentials (unrestricted)**).
5. In the left sidebar, click **+ Add Credentials**.

---

### Step 3.2: Enter Credential Parameters

Fill out the form with the following values:

| Field | Setting / Value | Explanation |
| :--- | :--- | :--- |
| **Kind** | **Username with password** | Standard HTTP/Token authentication for container registries. |
| **Scope** | **Global (Jenkins, nodes, items, all child items)** | Enables access by pipeline jobs. |
| **Username** | `<YOUR_DOCKERHUB_USERNAME>` | Your Docker Hub account handle (e.g., `rohankhadye11`). |
| **Password** | `<YOUR_DOCKERHUB_PASSWORD_OR_PAT>` | Your Docker Hub account password or Personal Access Token (PAT). *(Recommended: Docker Hub PAT with read/write permissions)*. |
| **ID** | **`dockerhub-creds`** | **CRITICAL:** Must match the `credentialsId` specified in the `Jenkinsfile` exactly. |
| **Description** | `Docker Hub credentials for FestPulse image push` | Human-readable identifier. |

> [!IMPORTANT]
> **Generating a Docker Hub Personal Access Token (PAT):**
> 1. Log in to [https://hub.docker.com/](https://hub.docker.com/).
> 2. Click your avatar &rarr; **Account Settings** &rarr; **Security** &rarr; **New Access Token**.
> 3. Description: `Jenkins FestPulse Pipeline`, Permissions: `Read & Write`.
> 4. Copy the token and paste it into the **Password** field in Jenkins.

6. Click **Create**. The credential `dockerhub-creds` is now active and protected by Jenkins credential masking.

---

## 4. Pipeline Execution & Continuous Deployment Walkthrough

Once `dockerhub-creds` is created, follow this sequence to trigger and verify the continuous deployment pipeline:

### Step 4.1: Commit and Push Updated Pipeline Configuration

```powershell
# Check git working directory status
git status

# Stage the updated Dockerfile, Jenkinsfile, and Phase 4 docs
git add Dockerfile Jenkinsfile docs/07_Docker_Lifecycle_Guide.md docs/08_Docker_Jenkins_CD_Guide.md

# Commit the Phase 4 enhancements
git commit -m "feat(cd): integrate Docker containerization and automated Jenkins CD pipeline"

# Push to GitHub
git push origin main
```

---

### Step 4.2: Trigger the Build in Jenkins

1. Navigate to the job page:  
   [http://localhost:9090/job/cultural-festival-platform-pipeline/](http://localhost:9090/job/cultural-festival-platform-pipeline/)
2. Click **Build with Parameters** in the left sidebar.
3. Select `DEPLOY_ENV=production` (or `staging`) and verify `RUN_TESTS=true`.
4. Click the blue **Build** button.

---

### Step 4.3: Monitor Pipeline Stage Progression

Watch the Pipeline Stage View as the execution proceeds across all 7 stages:

```text
[Checkout] ===> [Build] ===> [Unit & UI Tests] ===> [Package] ===> [Build Docker Image] ===> [Push Docker Image] ===> [Deploy Container]
   GREEN           GREEN             GREEN             GREEN               GREEN                  GREEN                 GREEN
```

* **Stage 5 Output Preview:**
  ```text
  Building Docker image: festpulse-app:latest and festpulse-app:6...
  Successfully tagged festpulse-app:latest
  Successfully tagged festpulse-app:6
  ```
* **Stage 6 Output Preview:**
  ```text
  Login Succeeded
  The push refers to repository [docker.io/rohankhadye11/festpulse-app]
  latest: digest: sha256:7b9c... size: 1782
  6: digest: sha256:7b9c... size: 1782
  ```
* **Stage 7 Output Preview:**
  ```text
  Stopping and removing existing container (if present)...
  Running new container instance...
  61f4c5409a39...
  Application container initialized and serving traffic.
  ```

---

### Step 4.4: Verify the Deployed Application

Once the pipeline completes successfully, verify that the application is running live inside Docker on port `8080`:

```powershell
# 1. Verify container status
docker ps -f name=festpulse-app

# 2. Check containerized Spring Boot logs
docker logs --tail 30 festpulse-app

# 3. Query the live health check endpoint
curl -i http://localhost:8080/api/v1/health

# 4. Open the interactive festival dashboard in your browser
Start-Process "http://localhost:8080"
```

---

## 5. Troubleshooting CD Pipeline Issues

### Issue 1: `docker login` fails with 401 Unauthorized
* **Cause:** The Docker Hub username or password/PAT in `dockerhub-creds` is incorrect or expired.
* **Solution:** Navigate to **Manage Jenkins** &rarr; **Credentials**, edit `dockerhub-creds`, and re-paste the freshly generated Docker Hub Personal Access Token.

### Issue 2: `docker: command not found` inside Jenkins Agent
* **Cause:** If Jenkins is running inside a container, it needs access to the host's Docker socket (`/var/run/docker.sock`).
* **Solution:** Verify the Jenkins container was started with Docker socket mounting or the Docker CLI plugin:
  ```bash
  docker run -d --name festpulse-jenkins -p 9090:8080 -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts
  ```

### Issue 3: Port 8080 already in use
* **Cause:** A previous local Java process or test container is still bound to host port `8080`.
* **Solution:** Run `docker rm -f festpulse-app` or stop any background Java processes before triggering deployment.
