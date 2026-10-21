# Automated Cultural Festival Schedule Platform (FestPulse)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-007396?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Embedded Tomcat](https://img.shields.io/badge/Server-Tomcat%2010.x%20(Port%208080)-F8DC75?logo=apachetomcat&logoColor=black)](https://tomcat.apache.org/)
[![Build Tool](https://img.shields.io/badge/Build-Maven%203.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

---

## 1. Project Overview

The **Automated Cultural Festival Schedule Platform (FestPulse)** is an enterprise university DevOps engineering project. It solves the critical operational challenge of scheduling multi-day, campus-wide cultural festivals involving thousands of student participants, hundreds of events, and shared university auditoriums.

By replacing chaotic spreadsheets, paper signups, and unstructured group messaging with an automated, transactional schedule coordination platform, FestPulse guarantees:
- **Zero Venue Collisions:** Mathematical conflict detection engine prevents overlapping bookings at physical venues:
  $$\max(S_1, S_2) < \min(E_1, E_2)$$
- **Role-Based Access Control (RBAC):** Strict operational segregation across `STUDENT`, `ORGANIZER`, and `FACULTY_ADMIN`.
- **Hard Capacity Capping:** Fire-code venue occupancy enforcement for indoor event registrations.
- **Embedded Web Server:** Pre-configured embedded Apache Tomcat container listening on port `8080`.

---

## 2. Project Documentation Index

All architectural specifications, Agile project management plans, Git collaboration procedures, and Mermaid visual models are cataloged below:

| Deliverable File | Description |
| :--- | :--- |
| 📄 [`docs/01_Problem_Scope_MVP.md`](docs/01_Problem_Scope_MVP.md) | Problem definition, target personas, stakeholder matrix, constraints, and the **15-Task MVP Scope Freeze**. |
| 📋 [`docs/02_Agile_DevOps_Plan.md`](docs/02_Agile_DevOps_Plan.md) | Agile Scrum workflow, 6-stage **Definition of Done (DoD)**, 15 User Stories with Acceptance Criteria, and Sprint roadmap. |
| 📐 [`docs/03_Architecture_SRS.md`](docs/03_Architecture_SRS.md) | Software Requirements Specification (SRS), multi-tier architecture, PostgreSQL data schema, conflict algorithm, and REST API directory. |
| 🛠️ [`docs/04_Git_Collaboration_Guide.md`](docs/04_Git_Collaboration_Guide.md) | **Phase 2 Git Handbook:** Step-by-step terminal commands for feature branching, deliberate merge conflict creation/resolution, and release tagging (`v1.0.0-MVP`). |
| 🔄 [`docs/diagrams/devops_workflow.md`](docs/diagrams/devops_workflow.md) | Mermaid.js diagram illustrating the 4-phase DevOps lifecycle and CI/CD quality gate pipeline. |
| 👤 [`docs/diagrams/use_case.md`](docs/diagrams/use_case.md) | Mermaid.js Use Case diagram and Actor-to-Capability matrix. |
| 🏛️ [`docs/diagrams/architecture.md`](docs/diagrams/architecture.md) | Mermaid.js multi-tier component architecture and sequence flow diagram. |

---

## 3. Project File Structure

```
cultural-festival-platform/
├── .gitignore                                     # Standard Git exclusions for Java/Maven/IDE
├── docs/
│   ├── 01_Problem_Scope_MVP.md                    # Formal problem scope & 15-task MVP freeze
│   ├── 02_Agile_DevOps_Plan.md                    # Scrum backlog, user stories & Definition of Done
│   ├── 03_Architecture_SRS.md                     # SRS summary, data model & REST API specification
│   ├── 04_Git_Collaboration_Guide.md              # Branching, conflict resolution & tagging SOP
│   └── diagrams/
│       ├── devops_workflow.md                     # DevOps lifecycle & CI/CD pipeline (Mermaid)
│       ├── use_case.md                            # Actor use cases & capabilities (Mermaid)
│       └── architecture.md                        # Multi-tier system architecture & sequences (Mermaid)
├── src/
│   ├── main/
│   │   ├── java/com/festival/schedule/
│   │   │   ├── FestivalScheduleApplication.java   # Spring Boot main runner & banner
│   │   │   ├── controller/
│   │   │   │   ├── EventController.java           # Event CRUD, scheduling & summary dashboard
│   │   │   │   ├── HealthController.java          # Diagnostic probe (/api/v1/health)
│   │   │   │   └── VenueController.java           # Venue CRUD endpoints (/api/v1/venues)
│   │   │   ├── exception/
│   │   │   │   ├── ErrorResponse.java             # Standardized JSON error response DTO
│   │   │   │   ├── GlobalExceptionHandler.java    # RestControllerAdvice mapping 404, 409, 400
│   │   │   │   ├── ResourceNotFoundException.java # HTTP 404 domain exception
│   │   │   │   └── ScheduleConflictException.java # HTTP 409 conflict exception
│   │   │   ├── model/
│   │   │   │   ├── Event.java                     # Event JPA entity
│   │   │   │   ├── EventCategory.java             # Category master entity
│   │   │   │   ├── EventStatus.java               # Lifecycle state enum
│   │   │   │   ├── User.java                      # User JPA entity
│   │   │   │   ├── UserRole.java                  # RBAC enum (STUDENT, ORGANIZER, ADMIN)
│   │   │   │   └── Venue.java                     # Physical facility entity
│   │   │   ├── repository/
│   │   │   │   ├── EventCategoryRepository.java   # Category JPA repository
│   │   │   │   ├── EventRepository.java           # Conflict detection JPQL & dynamic filters
│   │   │   │   ├── UserRepository.java            # User credentials & lookup queries
│   │   │   │   └── VenueRepository.java           # Venue master repository
│   │   │   └── service/
│   │   │       ├── ConflictDetectionEngine.java   # Core mathematical overlap engine
│   │   │       ├── EventService.java              # Event lifecycle, booking & summary metrics
│   │   │       └── VenueService.java              # Complete venue CRUD business logic
│   │   └── resources/
│   │       ├── application.properties             # Database placeholders & port 8080
│   │       └── static/
│   │           └── index.html                     # Embedded landing page & probe dashboard
│   └── test/
│       └── java/com/festival/schedule/
│           └── FestivalScheduleApplicationTests.java # Unit tests for Conflict Engine, Venue, Event
├── pom.xml                                        # Maven project configuration (Java 21)
└── README.md                                      # Primary documentation & setup guide
```

---

## 4. Quick Start & Local Execution

### Prerequisites
1. **Java Development Kit (JDK):** Version 21 LTS installed (`java -version`).
2. **PostgreSQL Server:** Version 15+ running locally or in Docker.
3. **Maven:** Version 3.9+ (or use IDE Maven integration).

---

### Step 1: Start PostgreSQL Database
```bash
docker run --name festival-postgres \
  -e POSTGRES_DB=festival_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15-alpine
```

---

### Step 2: Configure Database Credentials (if needed)
Edit `src/main/resources/application.properties` to match your local setup:
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/festival_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

---

### Step 3: Run the Application Locally
Using standard Maven CLI:
```bash
cd cultural-festival-platform
mvn clean compile
mvn spring-boot:run
```

Or run `FestivalScheduleApplication.java` directly in your favorite IDE (IntelliJ IDEA, Eclipse, VS Code).

---

### Step 4: Verify the Running Application Endpoints

1. **Interactive Web Dashboard:**  
   Navigate to: [http://localhost:8080/](http://localhost:8080/)
2. **DevOps Health Probe:**  
   ```bash
   curl http://localhost:8080/api/v1/health
   ```
3. **Venue Management CRUD:**  
   ```bash
   # List all venues
   curl http://localhost:8080/api/v1/venues

   # Create a venue
   curl -X POST http://localhost:8080/api/v1/venues \
     -H "Content-Type: application/json" \
     -d '{"name": "Main Auditorium", "building": "Academic Complex A", "capacity": 1200, "facilities": "Sound System, Projector"}'
   ```
4. **Operations Summary Dashboard:**  
   ```bash
   curl http://localhost:8080/api/v1/events/summary
   ```
   **Expected JSON Response:**
   ```json
   {
     "totalEvents": 0,
     "statusBreakdown": {
       "APPROVED": 0,
       "PENDING_APPROVAL": 0,
       "DRAFT": 0,
       "REJECTED": 0,
       "CANCELLED": 0
     },
     "totalApproved": 0,
     "totalPendingApproval": 0,
     "totalRejected": 0,
     "totalVenuesConfigured": 1,
     "activeVenuesCount": 1
   }
   ```
5. **Master Festival Events Feed:**  
   ```bash
   curl http://localhost:8080/api/v1/events
   ```

---

### Step 5: Run Automated Unit Tests
To execute the automated test suite verifying conflict detection algorithms, Venue CRUD, and Event summary calculations:
```bash
mvn test
```

---

## 5. Technology Stack Summary

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Java 21 (LTS) | Type-safe, high-performance modern Java features |
| **Framework** | Spring Boot 3.3.4 | Dependency injection, autoconfiguration, metrics |
| **Web Container** | Embedded Apache Tomcat 10.x | Standalone servlet engine on port `8080` |
| **Persistence** | Spring Data JPA / Hibernate ORM | Relational abstraction, JPQL, dynamic queries |
| **Database** | PostgreSQL 15+ | ACID relational storage, indexing, constraints |
| **Connection Pool**| HikariCP | High-performance enterprise JDBC connection pool |
| **Testing** | JUnit 5 & Mockito | Automated unit & integration verification |
| **Build & Package**| Apache Maven | Build lifecycle, dependency management, runnable JAR |

---

## 6. Engineering Roadmap

- **Phase 1 (Completed):** System scaffolding, domain entities, database configuration, conflict detection math engine, and architectural documentation.
- **Phase 2 (Completed):** Source code management plan, Venue CRUD APIs, Event scheduling conflict engine, summary dashboard, and release baseline tagging guide (`v1.0.0-MVP`).
- **Phase 3 (Upcoming):** Automated CI/CD pipeline via GitHub Actions, Docker multi-stage containerization, and staging deployment.
