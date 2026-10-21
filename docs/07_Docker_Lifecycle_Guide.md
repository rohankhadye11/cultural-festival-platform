# 07. Docker Container Lifecycle & Operations Guide

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Container Operations & Lifecycle Manual  
**Phase:** Phase 4 - Containerization & Continuous Deployment  
**Academic Mapping:** Task 11 (Containerization using Docker)  
**Audience:** DevOps Engineers, Cloud Administrators, and Evaluators  
**Document Version:** 4.0.0-RELEASE  
**Date:** October 2026  

---

## 1. Executive Summary & Container Architecture

Task 11 containerizes the Spring Boot 3.3.4 (Java 21) FestPulse platform into a production-grade, hardened Docker container. Containerization encapsulates the Java runtime, application bytecode, dependencies, and configuration into an immutable, portable artifact that runs identically across development, CI/CD runners, staging, and cloud production environments.

```
+-------------------------------------------------------------------------------------------------+
|                                 FESTPULSE DOCKER CONTAINER ARCHITECTURE                          |
+-------------------------------------------------------------------------------------------------+
|                                                                                                 |
|   +-----------------------------------------------------------------------------------------+   |
|   | Host Environment (Windows / Linux / Cloud VM)                                           |   |
|   | Port Mapping: Host 8080 <=======> Container 8080                                        |   |
|   +-----------------------------------------------------------------------------------------+   |
|                                            |                                                    |
|                                            v                                                    |
|   +-----------------------------------------------------------------------------------------+   |
|   | Container Instance: `festpulse-app`                                                     |   |
|   |                                                                                         |   |
|   |   +---------------------------------------------------------------------------------+   |   |
|   |   | Security Sandbox: Non-Root User (`spring:spring`, UID/GID 100)                  |   |   |
|   |   +---------------------------------------------------------------------------------+   |   |
|   |   | Application Layer: `/app/app.jar` (Executable Spring Boot Fat JAR)              |   |   |
|   |   +---------------------------------------------------------------------------------+   |   |
|   |   | JVM Runtime: Eclipse Temurin OpenJDK 21 JRE (`-XX:+UseContainerSupport`)        |   |   |
|   |   +---------------------------------------------------------------------------------+   |   |
|   |   | Base OS: Alpine Linux 3.20 (Minimal Attack Surface, ~180MB total size)          |   |   |
|   |   +---------------------------------------------------------------------------------+   |   |
|   +-----------------------------------------------------------------------------------------+   |
+-------------------------------------------------------------------------------------------------+
```

---

## 2. Dockerfile Design & Security Rationale

The root [Dockerfile](file:///c:/Users/Rohan/Documents/DevOps%20Project/cultural-festival-platform/Dockerfile) incorporates three cloud-native security and optimization best practices:

1. **Minimal Base Image (`eclipse-temurin:21-jre-alpine`):**
   - Uses Alpine Linux with the Just-in-Time JRE runtime (not full JDK).
   - Reduces final image size from ~550 MB (Ubuntu/Debian) down to ~180 MB, accelerating image pull times and minimizing security CVE vulnerabilities.
2. **Dedicated Non-Root User (`spring:spring`):**
   - In accordance with CIS Docker Benchmark 4.1, containers must never run as `root`.
   - The user `spring` and group `spring` isolate runtime processes, preventing container escape privileges.
3. **Container Resource Awareness:**
   - JVM flags `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage=75.0` ensure the JVM respects cgroup memory limits in containerized orchestration (Kubernetes / Docker Swarm).

---

## 3. Step-by-Step Terminal Lifecycle Guide (Task 11)

Follow these manual steps inside your project terminal (`c:\Users\Rohan\Documents\DevOps Project\cultural-festival-platform`).

### Step 3.1: Verify Pre-Requisite JAR Build

Before building the Docker image, ensure the packaged Spring Boot fat JAR exists in `target/`:

```powershell
# Set JAVA_HOME for the Maven Wrapper (if not set globally)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Package the application to generate the executable fat JAR in target/
.\mvnw.cmd clean package -DskipTests

# Verify that the generated JAR file exists
Get-ChildItem target\*.jar
```

*Expected Output:*
```text
    Directory: C:\Users\Rohan\Documents\DevOps Project\cultural-festival-platform\target

Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
-a----         21-10-2026    14:22       47659124 cultural-festival-platform-0.0.1-SNAPSHOT.jar
```

---

### Step 3.2: Build the Docker Image

Build the Docker image using the root `Dockerfile`. Tag it with both `latest` and a semantic version tag `1.0.0`:

```powershell
docker build -t festpulse-app:latest -t festpulse-app:1.0.0 .
```

**Expected Output:**
```text
[+] Building ...
 => [internal] load build definition from Dockerfile
 => [internal] load .dockerignore
 => [1/4] FROM docker.io/library/eclipse-temurin:21-jre-alpine
 => [2/4] RUN addgroup -S spring && adduser -S spring -G spring
 => [3/4] WORKDIR /app
 => [4/4] COPY target/*.jar app.jar
 => naming to docker.io/library/festpulse-app:latest
 => naming to docker.io/library/festpulse-app:1.0.0
```

---

### Step 3.3: Inspect the Generated Docker Image

Verify that the image is registered in your local Docker daemon and inspect its optimized size:

```powershell
# List the image and verify size
docker images festpulse-app

# Inspect image architecture and non-root user configuration
docker inspect --format='{{.Config.User}}' festpulse-app:latest
```

*Expected User Output:* `spring:spring`

---

### Step 3.4: Run the Application Container

Run the container in detached mode (`-d`), naming it `festpulse-app`, mapping host port `8080` to container port `8080`:

```powershell
docker run -d --name festpulse-app -p 8080:8080 --restart unless-stopped festpulse-app:latest
```

**Flags Breakdown:**
* `-d`: Detached mode (runs container in the background).
* `--name festpulse-app`: Assigns a predictable name for management commands.
* `-p 8080:8080`: Maps external traffic on port `8080` to Spring Boot inside the container.
* `--restart unless-stopped`: Ensures high availability; restarts on daemon reboot.

---

### Step 3.5: Inspect Container Runtime Status & Live Logs

Monitor the container startup process and stream application logs:

```powershell
# Verify container is in 'Up' status
docker ps -f name=festpulse-app

# Follow live Spring Boot startup logs
docker logs -f festpulse-app
```

Press `Ctrl + C` to stop following the log output once the Spring Boot startup banner completes:
```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.4)

2026-10-21 14:20:00 [main] INFO  c.f.s.FestivalScheduleApplication - Started FestivalScheduleApplication in 3.42 seconds
```

---

### Step 3.6: Verify Health and REST Endpoints

Confirm that the containerized application is serving incoming HTTP traffic:

```powershell
# Test health endpoint using PowerShell Invoke-RestMethod
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health"

# Test venue catalog endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/venues"
```

*Expected JSON Health Response:*
```json
{
  "status": "UP",
  "application": "FestPulse Cultural Festival Schedule Platform",
  "version": "1.0.0",
  "timestamp": "2026-10-21T14:20:05Z"
}
```

---

### Step 3.7: Stop the Container

Temporarily stop the running container:

```powershell
docker stop festpulse-app
```

Verify that the container has transitioned to `Exited` status:
```powershell
docker ps -a -f name=festpulse-app
```

---

### Step 3.8: Restart the Container

Restart the stopped container instance:

```powershell
docker start festpulse-app

# Alternatively, issue a hot restart
docker restart festpulse-app
```

---

### Step 3.9: Remove the Container

Stop and remove the container instance to free up host port `8080`:

```powershell
# Force remove container immediately
docker rm -f festpulse-app
```

Confirm removal:
```powershell
docker ps -a -f name=festpulse-app
```

---

### Step 3.10: Image Management & Pruning

To keep your local disk tidy and free of dangling intermediate image layers:

```powershell
# Remove a specific version tag
docker rmi festpulse-app:1.0.0

# Prune unused dangling build cache and dangling layers
docker image prune -f

# Inspect total Docker disk usage
docker system df
```

---

## 4. Quick Reference Cheat Sheet (Task 11 Commands)

| Operation | Command Line String | Purpose |
| :--- | :--- | :--- |
| **Build Image** | `docker build -t festpulse-app:latest .` | Compiles Dockerfile into immutable image |
| **Run Container** | `docker run -d --name festpulse-app -p 8080:8080 festpulse-app:latest` | Launches daemon mapped to port 8080 |
| **Live Logs** | `docker logs -f festpulse-app` | Streams stdout/stderr from Spring Boot |
| **Check Health** | `curl http://localhost:8080/api/v1/health` | Verifies containerized HTTP service |
| **Stop** | `docker stop festpulse-app` | Sends `SIGTERM` followed by graceful shutdown |
| **Restart** | `docker restart festpulse-app` | Reboots existing container process |
| **Remove** | `docker rm -f festpulse-app` | Force unbinds and removes container |
| **System Clean** | `docker system prune -f` | Cleans dangling layers and build caches |
