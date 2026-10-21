# 10. Site Reliability Engineering (SRE) & Automated Rollback Guide

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Disaster Recovery, Incident Response & Rollback Runbook  
**Phase:** Phase 5 - Infrastructure as Code, Configuration Management & Reliability  
**Academic Mapping:** Task 14 (Automated Rollback & Disaster Recovery)  
**Audience:** Site Reliability Engineers (SREs), DevOps Architects, and Evaluators  
**Document Version:** 5.0.0-RELEASE  
**Date:** October 2026  

---

## 1. Executive Summary & SRE Reliability Philosophy

In modern Site Reliability Engineering (SRE), failure is treated as an inevitable operational condition rather than an exceptional circumstance. When a new deployment exhibits performance degradation, database synchronization errors, or service crash loops, the primary directive is:

> **"Restore service first; investigate root causes second."**

The **FestPulse Automated Rollback Strategy** leverages Ansible variable overrides to revert production workloads to the Last Known Stable State (LKSS) within seconds, keeping **Mean Time to Recovery (MTTR)** under 15 seconds.

```
+---------------------------------------------------------------------------------------------------+
|                              DISASTER RECOVERY & ROLLBACK ARCHITECTURE                             |
+---------------------------------------------------------------------------------------------------+
|                                                                                                   |
|   [Normal State]                                                                                  |
|   Production Traffic ===> `festpulse-app:latest` (Port 8080)                                      |
|                                |                                                                  |
|                                v                                                                  |
|   [Incident Triggered]                                                                            |
|   High Error Rate / Regression Detected in `latest` (5xx Server Errors or CrashLoop)              |
|                                |                                                                  |
|                                v                                                                  |
|   [Automated Rollback Execution]                                                                  |
|   Ansible Playbook Invocation with Extra Vars: `-e "image_tag=1.0.0"`                             |
|                                |                                                                  |
|                                v                                                                  |
|   [Execution Steps]                                                                               |
|   1. Pulls / Validates Stable Image: `festpulse-app:1.0.0`                                        |
|   2. Idempotently terminates failing `festpulse-app:latest` container                             |
|   3. Deploys stable `festpulse-app:1.0.0` mapped to port 8080                                     |
|   4. Automated Health Probe confirms `/api/v1/health` returns HTTP 200 OK                         |
|                                |                                                                  |
|                                v                                                                  |
|   [Recovered State]                                                                               |
|   Production Restored (MTTR < 15s) ===> Post-Mortem Analysis in Staging / Sandbox                |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. The Rollback Mechanism: Ansible Variable Precedence

The Ansible playbook [ansible/deploy-festpulse.yml](file:///c:/Users/Rohan/Documents/DevOps%20Project/cultural-festival-platform/ansible/deploy-festpulse.yml) defines default variables:

```yaml
vars:
  image_name: "festpulse-app"
  image_tag: "latest"
```

In Ansible's architecture, **Extra Variables (`-e` or `--extra-vars`)** hold the **highest level of precedence** in the entire variable hierarchy. Supplying `-e "image_tag=1.0.0"` overrides any playbook default, dynamically directing the entire deployment logic (image pull, container startup, environment configuration, and health probing) to target the stable `1.0.0` artifact without modifying a single line of code.

---

## 3. Step-by-Step Rollback Execution (Task 14)

Follow these manual steps from your Windows terminal (`c:\Users\Rohan\Documents\DevOps Project\cultural-festival-platform`).

### Step 3.1: Execute Instant Rollback Command

Execute the Ansible playbook with the `-e "image_tag=1.0.0"` extra variable override:

#### Option A: Running on Windows via Dockerized Ansible Runner (Recommended)

```powershell
# Navigate to the project root
cd "c:\Users\Rohan\Documents\DevOps Project\cultural-festival-platform"

# Execute Instant Rollback to version 1.0.0
docker run --rm -it `
  -v "${PWD}:/workspace" `
  -v //var/run/docker.sock:/var/run/docker.sock `
  -w /workspace/ansible `
  cytopia/ansible:latest `
  ansible-playbook -i inventory.ini deploy-festpulse.yml -e "image_tag=1.0.0"
```

#### Option B: Running on WSL2 (Ubuntu / Debian)

```bash
cd /mnt/c/Users/Rohan/Documents/DevOps\ Project/cultural-festival-platform/ansible

# Execute Rollback
ansible-playbook -i inventory.ini deploy-festpulse.yml -e "image_tag=1.0.0"
```

---

### Step 3.2: Observe the Rollback Output

Notice how Ansible seamlessly swaps the container image and validates the restored service:

```text
TASK [1. Display deployment target and image parameters] ***********************************
ok: [localhost] => {
    "msg": [
        "==========================================================",
        " Starting FestPulse Deployment: festpulse-app",
        " Target Node:      localhost",
        " Docker Image:     festpulse-app:1.0.0",
        " Host Port:        8080",
        " App Directory:    /opt/festpulse",
        "=========================================================="
    ]
}

...

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
        " Active Image:    festpulse-app:1.0.0",
        " Health Endpoint: http://localhost:8080/api/v1/health",
        " HTTP Status:     200",
        " System Status:   UP",
        "=========================================================="
    ]
}

PLAY RECAP *********************************************************************************
localhost                  : ok=11   changed=3    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

---

## 4. Post-Rollback System Verification

Confirm from PowerShell that the live container is running the rolled-back version and serving traffic cleanly:

```powershell
# 1. Verify that the running container is using the 1.0.0 image
docker inspect --format='{{.Config.Image}}' festpulse-app
# Expected Output: festpulse-app:1.0.0

# 2. Check live health endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health"

# 3. Verify recent container log output
docker logs --tail 20 festpulse-app
```

---

## 5. Reliability Metrics & Incident Post-Mortem

### Reliability Key Performance Indicators (KPIs)

| Metric | Target SLA | FestPulse Metric with Ansible |
| :--- | :--- | :--- |
| **Mean Time to Detect (MTTD)** | < 1 minute | Continuous automated health check probes |
| **Mean Time to Recovery (MTTR)** | < 30 seconds | **~8 to 12 seconds** via Ansible one-command rollback |
| **Service Availability SLA** | 99.9% uptime | Preserved by instant rollback without host rebuilds |

---

### Incident Post-Mortem Checklist

Once production is stabilized via rollback, SREs follow the standard blameless post-mortem process:

1. **Isolate the Defective Image:**
   - Tag the failed artifact for forensic debugging:
     ```powershell
     docker tag festpulse-app:latest festpulse-app:failed-incident-$(Get-Date -Format 'yyyyMMdd')
     ```
2. **Reproduce in Isolated Sandbox:**
   - Launch the failed image in a non-production port (e.g., `-p 8085:8080`) to inspect exception stack traces without impacting live users.
3. **Draft Root Cause Analysis (RCA):**
   - Document: What triggered the failure? Why did automated unit/UI tests not catch it? What automated test will prevent recurrence?
4. **Permanent Remediation:**
   - Push a permanent bugfix PR through the standard 7-stage CI/CD pipeline.
