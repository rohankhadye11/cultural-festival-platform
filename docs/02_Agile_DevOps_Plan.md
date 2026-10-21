# 02. Agile & DevOps Plan

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Lifecycle Framework:** Scrum + Continuous Integration / Continuous Deployment (CI/CD)  
**Sprint Duration:** 2-Week Cadence (3 Sprints for Phase 1 MVP)  
**Document Version:** 1.0.0-RELEASE  
**Date:** October 2026  

---

## 1. Agile Methodology & Operating Model

The project adheres to an Agile-Scrum engineering framework coupled with DevOps automation. Rapid iterations allow cross-functional teams (developers, QA testers, and university domain coordinators) to continuously validate features against real operational requirements.

```
       +--------------------------------------------------------------+
       |               AGILE SCRUM SPRINT CEREMONIES (2 WEEKS)        |
       +--------------------------------------------------------------+
                                      |
     +-----------------+--------------+---------------+----------------+
     |                 |                              |                |
+----v-----+     +-----v------+                +------v-----+   +------v------+
| Sprint   |     | Daily      |                | Sprint     |   | Sprint      |
| Planning | --> | Standup    | (Iterate 10d)  | Review &   |-> | Retrospect  |
| (Day 1)  |     | (15 mins)  |                | Demo (D10) |   | (Day 10)    |
+----------+     +------------+                +------------+   +-------------+
```

### 1.1 Team Ceremonies & Cadence
- **Sprint Planning (Day 1):** Prioritize backlog items, estimate story points using Planning Poker, commit to Sprint Goal.
- **Daily Standup (15 min daily):** Synchronize on: (1) What did I complete yesterday? (2) What will I work on today? (3) Any blockers?
- **Backlog Refinement (Mid-sprint):** Decompose upcoming epic stories, clarify acceptance criteria with stakeholders.
- **Sprint Review & Stakeholder Demo (Day 10):** Demonstrate working software deployed on staging environment to faculty advisors and student heads.
- **Sprint Retrospective (Day 10):** Review process metrics, identify bottlenecks, define concrete action items for continuous improvement.

---

## 2. Standard "Definition of Done" (DoD)

A user story or backlog task is **ONLY** marked as **DONE** and eligible for release when it satisfies all criteria across the following six quality gates:

```
+---------------------------------------------------------------------------------------------------+
|                                  SIX-STAGE DEFINITION OF DONE (DoD)                                |
+---------------------------------------------------------------------------------------------------+
| [1] Code Quality    | Clean Java 21 code following Google Java Style Guide; zero SonarLint smells |
| [2] Unit Testing    | Minimum 80% line & branch coverage via JUnit 5 & Mockito; all tests pass     |
| [3] Integration Test| MockMvc / Testcontainers integration tests verifying HTTP endpoints & DB    |
| [4] Code Review     | Mandatory GitHub PR review & approval by >= 1 peer; no merge conflicts     |
| [5] CI Verification | Automated Maven build (`mvn clean verify`) passes green in GitHub Actions    |
| [6] Documentation   | OpenAPI / Swagger annotations added; markdown docs & changelog updated      |
+---------------------------------------------------------------------------------------------------+
```

### Detailed Quality Gate Checklist:
1. **Code Standards & Architecture Compliance:**
   - Adheres to standard layered package hierarchy (`controller`, `service`, `repository`, `model`).
   - Clean separation of business logic from HTTP transport layers.
   - Appropriate HTTP response codes (`200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`).
2. **Automated Testing Rigor:**
   - Every service method accompanied by unit tests mocking dependencies with `@Mock` and `@InjectMocks`.
   - Edge cases tested: null inputs, out-of-boundary time slots, duplicate names, database constraints.
3. **Security & Input Validation:**
   - All REST inputs validated using `@Valid` with Jakarta annotations (`@NotBlank`, `@Size`, `@Future`).
   - Zero SQL injection vulnerability (all queries parameterized through Spring Data JPA).
4. **Peer Review & Branch Protection:**
   - Direct commits to `main` and `develop` are strictly prohibited.
   - Feature branches merged exclusively via Pull Requests with a clean linear rebase or squash commit.
5. **Continuous Integration (CI):**
   - Headless Maven compilation succeeds with zero errors.
   - SonarQube quality gate rating of Grade 'A' for reliability and security.
6. **Deployment & Verification:**
   - Artifact packaged as an executable standalone Spring Boot JAR.
   - Successfully boots against PostgreSQL test instance without unhandled migration exceptions.

---

## 3. 15-Task Product Backlog & User Stories

### Story Points Reference (Modified Fibonacci Scale)
- `1` - Trivial change, configuration tweak, single-field addition.
- `2` - Simple CRUD endpoint with existing patterns.
- `3` - Standard business service logic with validation and tests.
- `5` - Complex logic involving multi-entity relationships or algorithmic validation.
- `8` - Cross-cutting module involving security, transaction boundaries, or state transitions.

---

### Module A: Core Infrastructure & Identity Governance

#### Task 01: Scaffold Spring Boot Base Application & Maven Setup
- **ID:** `TASK-01` | **Story Points:** 2 | **Priority:** Must Have | **Sprint:** 1
- **User Story:**  
  *As a DevOps Engineer, I want to scaffold a production-ready Spring Boot 3.x project with Maven and PostgreSQL dependencies, so that the development team has a standardized foundation for building features.*
- **Acceptance Criteria:**
  - **Given** a clean development environment with Java 21,
  - **When** the project is compiled using Maven,
  - **Then** all required dependencies (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `postgresql`, `spring-boot-starter-validation`, `spring-boot-starter-test`) resolve without conflict, and the embedded Tomcat server starts on port `8080`.

#### Task 02: Implement User Entity, Roles & Repository
- **ID:** `TASK-02` | **Story Points:** 3 | **Priority:** Must Have | **Sprint:** 1
- **User Story:**  
  *As a System Architect, I want to define the User entity and Role enumeration (`STUDENT`, `ORGANIZER`, `FACULTY_ADMIN`), so that identity and permissions can be persisted cleanly in PostgreSQL.*
- **Acceptance Criteria:**
  - **Given** the persistence context,
  - **When** a User entity is persisted,
  - **Then** it must enforce unique constraints on `username` and `email`, valid non-null roles, audit timestamps (`createdAt`), and provide a Spring Data JPA repository with `findByUsername` and `findByEmail`.

#### Task 03: Build Authentication & Registration REST Endpoints
- **ID:** `TASK-03` | **Story Points:** 5 | **Priority:** Must Have | **Sprint:** 1
- **User Story:**  
  *As a Student or Organizer, I want to register an account and log in securely, so that I can access personalized features based on my authorized role.*
- **Acceptance Criteria:**
  - **Given** valid registration details (`username`, `password`, `email`, `role`),
  - **When** `POST /api/v1/auth/register` is invoked,
  - **Then** the password is saved using BCrypt hashing, a `201 Created` is returned, and duplicate emails yield a `409 Conflict`.
  - **Given** valid credentials,
  - **When** `POST /api/v1/auth/login` is invoked,
  - **Then** a `200 OK` response with user identity and assigned role token is returned.

---

### Module B: Venue & Category Resource Management

#### Task 04: Implement Physical Venue Master Data Model & Repository
- **ID:** `TASK-04` | **Story Points:** 2 | **Priority:** Must Have | **Sprint:** 1
- **User Story:**  
  *As a Facility Manager, I want physical campus venues (Auditoriums, Amphitheater, Studios) modeled in the database with seating capacity and technical equipment specifications, so that event locations can be booked systematically.*
- **Acceptance Criteria:**
  - **Given** a venue with name, building location, seating capacity (> 0), and facility tags (e.g., `["Sound System", "Projector", "Stage Lights"]`),
  - **When** saved to PostgreSQL,
  - **Then** the record is persisted with an active status flag and unique venue name constraint.

#### Task 05: Build Venue Management REST API
- **ID:** `TASK-05` | **Story Points:** 3 | **Priority:** Must Have | **Sprint:** 1
- **User Story:**  
  *As a Faculty Administrator, I want REST endpoints to create, update, list, and view campus venues, so that the festival facility catalog is accurately maintained.*
- **Acceptance Criteria:**
  - **Given** an authorized Administrator,
  - **When** `GET /api/v1/venues` is called,
  - **Then** all active venues are returned in a JSON array.
  - **When** `POST /api/v1/venues` is called with invalid negative capacity,
  - **Then** a `400 Bad Request` with field validation errors is returned.

#### Task 06: Implement Event Category Master Model & REST Endpoints
- **ID:** `TASK-06` | **Story Points:** 3 | **Priority:** Should Have | **Sprint:** 1
- **User Story:**  
  *As an Event Coordinator, I want to categorize events into standardized genres (Music, Dance, Drama, Literary, Fine Arts, Tech), so that schedules can be color-coded and filtered.*
- **Acceptance Criteria:**
  - **Given** category master requests (`name`, `description`, `colorCode`),
  - **When** `GET /api/v1/categories` or `POST /api/v1/categories` are executed,
  - **Then** categories are persisted and returned with hex color validation (e.g., `#FF5733`).

---

### Module C: Event Scheduling & Conflict Detection Engine

#### Task 07: Implement Event Entity with Lifecycle State Machine
- **ID:** `TASK-07` | **Story Points:** 3 | **Priority:** Must Have | **Sprint:** 2
- **User Story:**  
  *As a System Architect, I want an Event entity that models event timings, venue associations, organizer references, and lifecycle states (`DRAFT`, `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `CANCELLED`), so that event progress is tracked reliably.*
- **Acceptance Criteria:**
  - **Given** an event draft,
  - **When** transitioning between states,
  - **Then** invalid transitions (e.g., from `CANCELLED` directly to `APPROVED`) are rejected by domain business logic.

#### Task 08: Develop Core Schedule Conflict Engine (Temporal + Spatial Overlap Detection)
- **ID:** `TASK-08` | **Story Points:** 5 | **Priority:** Must Have | **Sprint:** 2
- **User Story:**  
  *As an Event Organizer, I want the system to mathematically verify that a proposed venue and time slot does not overlap with any existing approved event, so that venue collisions are completely prevented.*
- **Acceptance Criteria:**
  - **Given** an existing Event $A$ with time range $[S_A, E_A]$ at Venue $V$,
  - **When** a new Event $B$ with range $[S_B, E_B]$ is requested at Venue $V$,
  - **Then** the engine detects an overlap if $\max(S_A, S_B) < \min(E_A, E_B)$, and raises a `ScheduleConflictException` accompanied by details of the conflicting event.

#### Task 09: Build Event Creation & Updation REST Endpoints with Auto-Conflict Rejection
- **ID:** `TASK-09` | **Story Points:** 5 | **Priority:** Must Have | **Sprint:** 2
- **User Story:**  
  *As an Event Organizer, I want to create and edit event schedules via REST endpoints with automatic conflict checks, so that I can schedule events safely.*
- **Acceptance Criteria:**
  - **Given** valid event payload,
  - **When** `POST /api/v1/events` is submitted with conflicting venue timing,
  - **Then** the API returns HTTP `409 Conflict` with a detailed error payload indicating the clashing event ID and title.
  - **Given** non-conflicting timing,
  - **Then** the API returns HTTP `201 Created` with status `PENDING_APPROVAL`.

#### Task 10: Build Faculty Admin Approval & Rejection Workflow Endpoints
- **ID:** `TASK-10` | **Story Points:** 3 | **Priority:** Must Have | **Sprint:** 2
- **User Story:**  
  *As a Faculty Administrator, I want to approve or reject pending event proposals with review notes, so that the published festival schedule contains only authorized activities.*
- **Acceptance Criteria:**
  - **Given** an event with status `PENDING_APPROVAL`,
  - **When** `PATCH /api/v1/events/{id}/status` is called with action `APPROVE`,
  - **Then** the status updates to `APPROVED`, publishing it to the public timetable.
  - **When** called with action `REJECT` and a rejection reason,
  - **Then** status updates to `REJECTED` and the organizer is notified.

---

### Module D: Student Registration & Public Discovery

#### Task 11: Implement Public Filterable Festival Schedule Feed API
- **ID:** `TASK-11` | **Story Points:** 3 | **Priority:** Must Have | **Sprint:** 3
- **User Story:**  
  *As a Student Attendee, I want to query the master schedule with multi-criteria filters (date, venue, category, keyword), so that I can easily plan which events to attend.*
- **Acceptance Criteria:**
  - **Given** query parameters `?date=2026-11-15&categoryId=2&venueId=1`,
  - **When** `GET /api/v1/events` is called,
  - **Then** only matching `APPROVED` events are returned sorted chronologically by start time.

#### Task 12: Build Student Event Registration Workflow with Capacity Hard-Capping
- **ID:** `TASK-12` | **Story Points:** 5 | **Priority:** Must Have | **Sprint:** 3
- **User Story:**  
  *As a Student, I want to register for a limited-seat workshop or competition, so that my seat is confirmed without venue overcrowding.*
- **Acceptance Criteria:**
  - **Given** an approved event with maximum capacity $C$ and current registrations $R$,
  - **When** a student requests `POST /api/v1/events/{id}/register`,
  - **Then** if $R < C$, registration succeeds with HTTP `201 Created`; if $R \ge C$, the request is rejected with HTTP `422 Unprocessable Entity` ("Event Capacity Full").

#### Task 13: Build "My Itinerary" Personal Schedule API for Registered Students
- **ID:** `TASK-13` | **Story Points:** 2 | **Priority:** Should Have | **Sprint:** 3
- **User Story:**  
  *As a Student, I want to view a personalized timeline of all events I have registered for, so that I have a customized personal itinerary.*
- **Acceptance Criteria:**
  - **Given** an authenticated student,
  - **When** `GET /api/v1/users/me/schedule` is requested,
  - **Then** an ordered list of registered events with venue and timing details is returned.

---

### Module E: Operations Dashboard & Observability

#### Task 14: Build Central Operational Metrics Dashboard API
- **ID:** `TASK-14` | **Story Points:** 3 | **Priority:** Should Have | **Sprint:** 3
- **User Story:**  
  *As a Core Organizing Committee Member, I want a centralized dashboard API summarizing total events, venue occupancy rates, and registration counts, so that I can monitor festival operations in real time.*
- **Acceptance Criteria:**
  - **Given** festival data in PostgreSQL,
  - **When** `GET /api/v1/dashboard/stats` is called,
  - **Then** a JSON response containing total events by status, venue utilization percentages, and total registrations is returned within 200 ms.

#### Task 15: Implement Health Check, DB Connectivity Verification & Actuator Metrics
- **ID:** `TASK-15` | **Story Points:** 2 | **Priority:** Must Have | **Sprint:** 3
- **User Story:**  
  *As a DevOps Engineer, I want Spring Boot Actuator health check and database ping endpoints, so that monitoring systems can continuously verify platform availability.*
- **Acceptance Criteria:**
  - **Given** running Spring Boot instance,
  - **When** `GET /api/v1/health` or `GET /actuator/health` is invoked,
  - **Then** an HTTP `200 OK` with `{"status": "UP", "database": "CONNECTED", "timestamp": "..."}` is returned.

---

## 4. Sprint Breakdown & Release Roadmap

```
+-----------------------------------------------------------------------------------------------+
| SPRINT 1: Core Foundation & Master Data (Days 1 - 10)                                          |
| Goal: Production-ready Spring Boot scaffolding, PostgreSQL persistence, Users, Venues, & Auth |
+-----------------------------------------------------------------------------------------------+
| TASK-01: Spring Boot 3.x Scaffolding & Maven Config                                 [2 pts]   |
| TASK-02: User Entity, Roles (STUDENT, ORGANIZER, ADMIN) & Repository               [3 pts]   |
| TASK-03: Authentication & Registration REST Endpoints                               [5 pts]   |
| TASK-04: Venue Master Model & Repository                                            [2 pts]   |
| TASK-05: Venue Management REST API                                                  [3 pts]   |
| TASK-06: Event Category Master Model & REST API                                     [3 pts]   |
| Total Story Points: 18 pts                                                                    |
+-----------------------------------------------------------------------------------------------+

+-----------------------------------------------------------------------------------------------+
| SPRINT 2: Scheduling, State Machine & Conflict Engine (Days 11 - 20)                          |
| Goal: Event CRUD lifecycle, automated venue collision prevention, and faculty approval flows |
+-----------------------------------------------------------------------------------------------+
| TASK-07: Event Entity with Lifecycle State Machine                                  [3 pts]   |
| TASK-08: Core Schedule Conflict Engine (Temporal-Spatial Overlap)                   [5 pts]   |
| TASK-09: Event Creation/Edit REST API with Auto-Conflict Rejection                  [5 pts]   |
| TASK-10: Faculty Admin Approval / Rejection Workflow API                            [3 pts]   |
| Total Story Points: 16 pts                                                                    |
+-----------------------------------------------------------------------------------------------+

+-----------------------------------------------------------------------------------------------+
| SPRINT 3: Discovery, Registration, Dashboard & Observability (Days 21 - 30)                   |
| Goal: Student-facing discovery, capacity-capped booking, operational metrics, and monitoring  |
+-----------------------------------------------------------------------------------------------+
| TASK-11: Public Filterable Schedule Feed API                                        [3 pts]   |
| TASK-12: Student Registration Workflow with Capacity Hard-Capping                   [5 pts]   |
| TASK-13: Student "My Itinerary" Personal Schedule API                               [2 pts]   |
| TASK-14: Operational Metrics Dashboard API                                          [3 pts]   |
| TASK-15: Health Check & Actuator Observability                                      [2 pts]   |
| Total Story Points: 15 pts                                                                    |
+-----------------------------------------------------------------------------------------------+
```

---

## 5. Git Branching & DevOps CI/CD Integration

### 5.1 Branching Strategy (Trunk-Based with Protected Branches)
- `main`: Production-ready, deployable code. Direct pushes blocked.
- `develop`: Integration branch for current sprint.
- `feature/TASK-XX-<short-name>`: Short-lived developer branches cut from `develop`.
- `bugfix/TASK-XX-<short-name>`: Defect resolution branches.

### 5.2 Automated CI/CD Pipeline Stages
Every Pull Request automatically triggers the following pipeline:
1. **Lint & Static Analysis:** Checks code formatting and syntax cleanliness.
2. **Compile & Unit Test:** `mvn test` runs all JUnit 5 and Mockito suites.
3. **Integration Test:** `mvn verify` tests Spring Boot slices and database interactions.
4. **Artifact Build:** Standalone executable JAR produced.
5. **Staging Smoke Test:** Container deployment verification with `/api/v1/health`.
