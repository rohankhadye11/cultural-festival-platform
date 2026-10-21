# 04. Git Collaboration Guide: Branching, Conflict Resolution & Release Baseline

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Standard Operating Procedure (SOP) & Developer Handbook  
**Phase:** Phase 2 - Source Code Management & Core Development  
**Audience:** Development Team, DevOps Mentors, and University Evaluators  
**Document Version:** 2.0.0-RELEASE  
**Date:** October 2026  

---

> [!IMPORTANT]
> **CRITICAL REMOTE OPERATION POLICY:**  
> All automated agents and assistants are **strictly forbidden** from executing remote operations (`git push`, `git fetch`, `git pull`, or GitHub CLI commands). **All remote repository synchronization and GitHub Pull Requests must be performed manually by the developer** as guided in [Section 7](#7-manual-remote-synchronization--github-pull-requests-user-only).

---

## 1. Prerequisites & Terminal Environment Check

Ensure you are in the project root directory inside your Windows terminal (PowerShell or Git Bash):

```powershell
# Verify current working directory
cd "C:\Users\Rohan\Documents\DevOps Project\cultural-festival-platform"

# Verify Git is installed and check status
git --version
git status
```

> [!TIP]
> **PowerShell Note on Directory Cleanup:**  
> If you ever need to reset a local `.git` repository in Windows PowerShell, do not use `rmdir /s /q` or `rm -rf`. Instead, run:
> ```powershell
> Remove-Item -Recurse -Force .git
> ```

---

## 2. Step 1: Initial Repository Baseline on `main`

Ensure the initial Phase 1 foundation and the `.gitignore` are committed to the `main` branch before branching out.

```powershell
# 1. Initialize Git repository (if not already initialized)
git init

# 2. Configure local developer identity (if not globally set)
git config user.name "Your Name"
git config user.email "your.email@university.edu"

# 3. Stage the .gitignore and project files
git add .gitignore README.md pom.xml docs/ src/

# 4. Create the initial baseline commit on main
git commit -m "chore: scaffold Spring Boot 3.3 project structure, Phase 1 docs, and baseline configuration"

# 5. Verify the clean tree
git status
```

---

## 3. Step 2: Task 4 — Feature Branch `feature/venue-management`

In this step, you isolate all physical venue management capabilities (CRUD REST APIs, JPA repository, service, controller, and exception handling) on its own dedicated feature branch.

```powershell
# 1. Ensure you are branching from the clean main branch
git checkout main

# 2. Create and switch to the venue-management feature branch
git checkout -b feature/venue-management

# 3. Verify active branch (the asterisk marks current branch)
git branch

# 4. Stage all Venue-related source code and exception handlers
git add src/main/java/com/festival/schedule/model/Venue.java
git add src/main/java/com/festival/schedule/repository/VenueRepository.java
git add src/main/java/com/festival/schedule/service/VenueService.java
git add src/main/java/com/festival/schedule/controller/VenueController.java
git add src/main/java/com/festival/schedule/exception/

# 5. Commit the feature code with a conventional commit message
git commit -m "feat(venue): implement complete CRUD REST APIs, service validation, and exception handling"

# 6. Inspect the commit history on this branch
git log -n 1 --stat
```

---

## 4. Step 3: Task 5 — Feature Branch `feature/event-scheduling`

In this step, you switch back to `main` and branch out to implement the Core Event Scheduling logic, the mathematical Conflict Detection Engine, and the Operations Summary Dashboard.

```powershell
# 1. Return to the main branch
git checkout main

# 2. Create and switch to the event-scheduling feature branch
git checkout -b feature/event-scheduling

# 3. Verify active branch
git branch

# 4. Stage Event Scheduling, Conflict Engine, and Dashboard components
git add src/main/java/com/festival/schedule/model/Event.java
git add src/main/java/com/festival/schedule/model/EventStatus.java
git add src/main/java/com/festival/schedule/repository/EventRepository.java
git add src/main/java/com/festival/schedule/service/ConflictDetectionEngine.java
git add src/main/java/com/festival/schedule/service/EventService.java
git add src/main/java/com/festival/schedule/controller/EventController.java
git add src/test/java/com/festival/schedule/FestivalScheduleApplicationTests.java

# 5. Commit the event scheduling features
git commit -m "feat(events): implement conflict detection engine, booking workflows, and summary dashboard"

# 6. Verify commit history
git log -n 1 --oneline
```

---

## 5. Step 4: Task 6 — Deliberate Merge Conflict Creation & Resolution

A fundamental DevOps competency is understanding how Git handles concurrent edits on the same file lines, triggering conflicts, and resolving them cleanly without losing code.

Follow this deliberate, reproducible 5-step walkthrough:

```
                  +----------------------------------------------+
                  |               main branch                    |
                  |     server.port=8080 (initial baseline)      |
                  +----------------------------------------------+
                               /                     \
                              /                       \
        +----------------------------+   +----------------------------+
        |  conflict-demo-alpha       |   |  conflict-demo-beta        |
        |  Change server.port to 8081|   |  Change server.port to 9090|
        +----------------------------+   +----------------------------+
                              \                       /
                               \                     /
                                +-------------------+
                                |    git merge      |
                                |  MERGE CONFLICT!  |
                                +-------------------+
```

### Step 5.1: Create Branch A and Modify `application.properties`

```powershell
# 1. Branch from main
git checkout main
git checkout -b conflict-demo-alpha

# 2. Modify line 9 in src/main/resources/application.properties
# Change 'server.port=8080' to 'server.port=8081'
# In PowerShell, you can use the command below or edit directly in your IDE:
(Get-Content src/main/resources/application.properties) -replace 'server.port=8080', 'server.port=8081' | Set-Content src/main/resources/application.properties

# 3. Commit change on Branch A
git add src/main/resources/application.properties
git commit -m "chore(config): set server port to 8081 on demo branch alpha"
```

### Step 5.2: Create Branch B and Modify the Same Line Differently

```powershell
# 1. Switch back to main
git checkout main

# 2. Create Branch B
git checkout -b conflict-demo-beta

# 3. Modify line 9 in src/main/resources/application.properties to a different port (e.g. 9090)
(Get-Content src/main/resources/application.properties) -replace 'server.port=8080', 'server.port=9090' | Set-Content src/main/resources/application.properties

# 4. Commit change on Branch B
git add src/main/resources/application.properties
git commit -m "chore(config): set server port to 9090 on demo branch beta"
```

### Step 5.3: Trigger the Merge Conflict

```powershell
# 1. Switch back to Branch A
git checkout conflict-demo-alpha

# 2. Attempt to merge Branch B into Branch A
git merge conflict-demo-beta
```

**Expected Git Terminal Output:**
```
Auto-merging src/main/resources/application.properties
CONFLICT (content): Merge conflict in src/main/resources/application.properties
Automatic merge failed; fix conflicts and then commit the result.
```

### Step 5.4: Inspect the Conflict Markers

Open `src/main/resources/application.properties` in your IDE or inspect with terminal:

```properties
<<<<<<< HEAD
server.port=8081
=======
server.port=9090
>>>>>>> conflict-demo-beta
```

- Content between `<<<<<<< HEAD` and `=======` is what existed on `conflict-demo-alpha`.
- Content between `=======` and `>>>>>>> conflict-demo-beta` is what came from `conflict-demo-beta`.

### Step 5.5: Resolve and Commit the Conflict

Decide on the resolved configuration (maintain default `server.port=8080`):

```powershell
# 1. Edit src/main/resources/application.properties to remove all conflict markers.
# Replace the conflicted section with:
# server.port=8080

(Get-Content src/main/resources/application.properties) -replace '<<<<<<< HEAD[\s\S]*?>>>>>>> conflict-demo-beta', 'server.port=8080' | Set-Content src/main/resources/application.properties

# 2. Check git status to confirm conflict resolution
git status

# 3. Stage the resolved file
git add src/main/resources/application.properties

# 4. Complete the merge commit
git commit -m "fix(merge): resolve server port conflict between alpha (8081) and beta (9090) by restoring 8080 baseline"

# 5. Clean up temporary demo branches (optional)
git checkout main
git branch -D conflict-demo-alpha conflict-demo-beta
```

---

## 6. Step 5: Merge Features into `main` & Create Release Tag `v1.0.0-MVP`

Once feature branches pass unit tests (`mvn test`), merge them into `main` and tag the release-ready baseline:

```powershell
# 1. Switch to main
git checkout main

# 2. Merge feature/venue-management
git merge --no-ff feature/venue-management -m "merge: integrate feature/venue-management (CRUD APIs)"

# 3. Merge feature/event-scheduling
git merge --no-ff feature/event-scheduling -m "merge: integrate feature/event-scheduling (Conflict Engine & Dashboard)"

# 4. Stage any updated documentation
git add docs/04_Git_Collaboration_Guide.md
git commit -m "docs: finalize Git collaboration handbook and Phase 2 implementation guides"

# 5. Create an annotated Git release tag
git tag -a v1.0.0-MVP -m "Release v1.0.0-MVP: Phase 2 baseline with Venue CRUD, Conflict Detection Engine, and Summary Dashboard"

# 6. Verify the tag details
git tag -n
git show v1.0.0-MVP
```

---

## 7. Manual Remote Synchronization & GitHub Pull Requests (USER-ONLY)

> [!CAUTION]
> As specified in project constraints, **you (the developer) must execute all remote commands below manually in your terminal**.

### 7.1 Connect Local Repository to Your GitHub Remote

```powershell
# Replace <YOUR_GITHUB_USERNAME> with your actual GitHub username or organization:
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/cultural-festival-platform.git

# Verify remote configuration
git remote -v
```

### 7.2 Push Branches to GitHub

```powershell
# 1. Push main branch as upstream default
git push -u origin main

# 2. Push feature branches for code review
git push -u origin feature/venue-management
git push -u origin feature/event-scheduling

# 3. Push the release tag
git push origin v1.0.0-MVP
```

### 7.3 Create Pull Requests on GitHub

1. Navigate to your repository: `https://github.com/<YOUR_GITHUB_USERNAME>/cultural-festival-platform`.
2. Click **"Compare & pull request"** next to `feature/venue-management`.
3. Set base branch to `main` and compare to `feature/venue-management`.
4. Title: `feat(venue): implement complete CRUD REST APIs`.
5. Verify checks pass and click **"Merge pull request"**.
6. Repeat for `feature/event-scheduling`.

---

## 8. Summary of Git Commands Cheat Sheet

| Operation | PowerShell / Git Bash Command | Purpose |
| :--- | :--- | :--- |
| **Create Branch** | `git checkout -b <branch-name>` | Create & switch to feature branch |
| **List Branches** | `git branch -a` | List local and remote branches |
| **Stage Files** | `git add <files>` | Stage modifications for commit |
| **Commit** | `git commit -m "<message>"` | Commit staged snapshot locally |
| **Merge Branch** | `git merge --no-ff <branch>` | Merge branch keeping merge commit history |
| **Abort Merge** | `git merge --abort` | Abort an active merge conflict cleanly |
| **Create Tag** | `git tag -a <tag-name> -m "<msg>"` | Create annotated release baseline tag |
| **Push Branch** | `git push -u origin <branch>` | Push branch to GitHub (manual) |
| **Push Tags** | `git push origin --tags` | Push all local tags to GitHub (manual) |
