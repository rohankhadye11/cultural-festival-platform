# 09. Infrastructure as Code (IaC) & Ansible Provisioning Guide

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Configuration Management & Infrastructure as Code (IaC) Guide  
**Phase:** Phase 5 - Infrastructure as Code, Configuration Management & Reliability  
**Academic Mapping:** Task 13 (Automated Provisioning & Configuration with Ansible)  
**Audience:** DevOps Engineers, Site Reliability Engineers (SREs), and Academic Evaluators  
**Document Version:** 5.0.0-RELEASE  
**Date:** October 2026  

---

## 1. Executive Summary & IaC Architecture

Task 13 introduces **Infrastructure as Code (IaC)** and **Configuration Management** using Ansible. While Docker packages application code and dependencies into immutable containers, Ansible automates host-level provisioning, environment configuration, container lifecycle orchestration, and health probe verification across target environments.

```
+---------------------------------------------------------------------------------------------------+
|                              FESTPULSE ANSIBLE PROVISIONING FLOW (TASK 13)                        |
+---------------------------------------------------------------------------------------------------+
|                                                                                                   |
|   +-------------------------------------------------------------------------------------------+   |
|   | Ansible Controller (Windows via Docker / WSL2 / Linux Agent)                              |   |
|   | Command: `ansible-playbook -i inventory.ini deploy-festpulse.yml`                          |   |
|   +-------------------------------------------------------------------------------------------+   |
|                                                |                                                  |
|                                                v                                                  |
|   +-------------------------------------------------------------------------------------------+   |
|   | Target Host: [production]                                                                 |   |
|   |                                                                                           |   |
|   |   1. Prerequisite Packages ===> Ensures `curl` & `ca-certificates` are installed          |   |
|   |   2. App Directory Setup   ===> Creates `/opt/festpulse` (mode: 0755)                     |   |
|   |   3. Environment Config    ===> Deploys `/opt/festpulse/app.env`                          |   |
|   |   4. Docker Image Pull     ===> Pulls `festpulse-app:latest` (or specified tag)          |   |
|   |   5. Container Lifecycle   ===> Idempotently stops/removes old `festpulse-app`            |   |
|   |   6. Container Launch      ===> Runs new container mapped to port 8080                    |   |
|   |   7. Readiness Check       ===> Waits for port 8080 and probes `/api/v1/health`           |   |
|   +-------------------------------------------------------------------------------------------+   |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. Inventory & Playbook Structure

The Ansible configuration resides in the [ansible/](file:///c:/Users/Rohan/Documents/DevOps%20Project/cultural-festival-platform/ansible) directory:

```text
ansible/
├── inventory.ini           # Target host inventory defining the [production] group
└── deploy-festpulse.yml    # Idempotent orchestration playbook
```

### Key Playbook Variables ([ansible/deploy-festpulse.yml](file:///c:/Users/Rohan/Documents/DevOps%20Project/cultural-festival-platform/ansible/deploy-festpulse.yml))
* `app_name`: Container and service name (`festpulse-app`).
* `image_name`: Base repository image name (`festpulse-app`).
* `image_tag`: Target deployment version tag (defaults to `latest`, overridable via CLI extra vars).
* `host_port` / `app_port`: External host and container port mapping (`8080`).
* `app_dir`: Production host configuration directory (`/opt/festpulse`).
* `health_endpoint`: URL for automated health verification (`http://localhost:8080/api/v1/health`).

---

## 3. Step-by-Step Execution Guide on Windows

Because native Ansible is designed for Unix-like environments, Windows developers execute Ansible seamlessly via a **lightweight Docker container** or through **WSL2**.

### Option A: Running Ansible via Docker Container (Recommended)

Run the containerized Ansible CLI, mounting your project directory and the host Docker daemon socket:

```powershell
# Navigate to the project root directory
cd "c:\Users\Rohan\Documents\DevOps Project\cultural-festival-platform"

# Execute Ansible Playbook via containerized runner
docker run --rm -it `
  -v "${PWD}:/workspace" `
  -v //var/run/docker.sock:/var/run/docker.sock `
  -w /workspace/ansible `
  cytopia/ansible:latest `
  ansible-playbook -i inventory.ini deploy-festpulse.yml
```

> [!NOTE]
> If `cytopia/ansible:latest` is downloading for the first time, Docker will pull the image automatically (~200MB). Alternatively, `willhallonline/ansible:latest` or `alpine/ansible:latest` can be used identically.

---

### Option B: Running Ansible via WSL2 (Ubuntu / Debian)

If you have Windows Subsystem for Linux (WSL2) installed:

```bash
# Open WSL terminal in your repository directory
cd /mnt/c/Users/Rohan/Documents/DevOps\ Project/cultural-festival-platform/ansible

# Execute the playbook directly
ansible-playbook -i inventory.ini deploy-festpulse.yml
```

---

## 4. Expected Playbook Execution Output

Upon successful execution, Ansible displays task progression followed by the **PLAY RECAP**:

```text
PLAY [Provision Target Host and Deploy FestPulse Production Container] *********************

TASK [Gathering Facts] *********************************************************************
ok: [localhost]

TASK [1. Display deployment target and image parameters] ***********************************
ok: [localhost] => {
    "msg": [
        "==========================================================",
        " Starting FestPulse Deployment: festpulse-app",
        " Target Node:      localhost",
        " Docker Image:     festpulse-app:latest",
        " Host Port:        8080",
        " App Directory:    /opt/festpulse",
        "=========================================================="
    ]
}

TASK [2. Ensure prerequisite system packages are installed] ********************************
ok: [localhost]

TASK [3. Ensure dedicated application directory exists] ************************************
changed: [localhost]

TASK [4. Create runtime environment configuration file] ************************************
changed: [localhost]

TASK [5. Pull the specified Docker image] **************************************************
ok: [localhost]

TASK [6. Inspect existing application container state] *************************************
ok: [localhost]

TASK [7. Stop and remove existing application container (if present)] **********************
changed: [localhost]

TASK [8. Start the new FestPulse application container] ************************************
changed: [localhost]

TASK [9. Wait for application HTTP port to become responsive] ******************************
ok: [localhost]

TASK [10. Perform automated health check probe] ********************************************
ok: [localhost]

TASK [11. Output deployment verification details] ******************************************
ok: [localhost] => {
    "msg": [
        "==========================================================",
        " FestPulse Deployment Verification SUCCESSFUL!",
        " Active Image:    festpulse-app:latest",
        " Health Endpoint: http://localhost:8080/api/v1/health",
        " HTTP Status:     200",
        " System Status:   UP",
        "=========================================================="
    ]
}

PLAY RECAP *********************************************************************************
localhost                  : ok=11   changed=4    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

---

## 5. Demonstrating Idempotency (The Core DevOps Principle)

**Idempotency** is the defining characteristic of professional Configuration Management: executing an automation script multiple times on the same target produces the identical system state without introducing unintended side effects or configuration drift.

### How to Demonstrate Idempotency:

1. **First Playbook Run (Initial Provisioning):**
   - The directory `/opt/festpulse` is created &rarr; reported as `changed=1`.
   - The environment file `/opt/festpulse/app.env` is written &rarr; reported as `changed=1`.
   - Result: `changed=4`, system brought from clean state to desired state.

2. **Second Playbook Run (Immediate Re-Run):**
   Execute the exact same command immediately:
   ```powershell
   docker run --rm -it `
     -v "${PWD}:/workspace" `
     -v //var/run/docker.sock:/var/run/docker.sock `
     -w /workspace/ansible `
     cytopia/ansible:latest `
     ansible-playbook -i inventory.ini deploy-festpulse.yml
   ```

3. **Observe Idempotency Results:**
   - `TASK [3. Ensure dedicated application directory exists]` &rarr; reports **`ok`** (NOT `changed`), because the directory already exists with the correct permissions.
   - `TASK [4. Create runtime environment configuration file]` &rarr; reports **`ok`** (checksum unchanged).
   - `TASK [2. Ensure prerequisite system packages are installed]` &rarr; reports **`ok`**.
   - No unnecessary package reinstalls or file corruptions occur.

---

## 6. Verifying the Deployment

After Ansible reports `failed=0`, verify the running application from your Windows terminal:

```powershell
# 1. Inspect container running status
docker ps -f name=festpulse-app

# 2. Query the live health check
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health"

# 3. Query the venue API endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/venues"
```
