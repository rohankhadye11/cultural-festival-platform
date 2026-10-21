# 03. Architecture & Software Requirements Specification (SRS)

**Project:** Automated Cultural Festival Schedule Platform (FestPulse)  
**Document Type:** Architecture Design Document (ADD) & Software Requirements Specification (SRS)  
**Target Environment:** Java 21, Spring Boot 3.3.x, PostgreSQL 15+, Embedded Tomcat 10.x  
**Document Version:** 1.0.0-RELEASE  
**Date:** October 2026  

---

## 1. System Overview & SRS Summary

### 1.1 Purpose
This document provides the authoritative architectural blueprint and functional specifications for the **Automated Cultural Festival Schedule Platform (FestPulse)**. It guides backend engineers, QA automation engineers, DevOps teams, and university project evaluators in understanding the design principles, domain data models, and API interfaces for the Phase 1 MVP release.

### 1.2 System Scope
FestPulse is an enterprise-grade schedule automation engine engineered to eliminate scheduling overlaps, venue double-bookings, and communication silos during collegiate festivals. The platform provides a centralized, transactional REST API backend powered by Spring Boot and PostgreSQL, running inside an embedded Tomcat container on port `8080`.

### 1.3 Definitions, Acronyms, and Abbreviations
- **API:** Application Programming Interface
- **DTO:** Data Transfer Object
- **JPA:** Java Persistence API
- **ORM:** Object-Relational Mapping (Hibernate)
- **RBAC:** Role-Based Access Control
- **REST:** Representational State Transfer
- **SRS:** Software Requirements Specification
- **Tomcat:** Apache Tomcat Embedded Servlet Container

---

## 2. Functional Requirements (FR) Specification

The Functional Requirements directly map to the 15 frozen MVP user stories:

| Requirement ID | Module | Feature Description | Authorized Roles |
| :--- | :--- | :--- | :--- |
| **FR-01** | Foundation | System must initialize via Spring Boot 3.x, configure HikariCP, and connect to PostgreSQL. | System / All |
| **FR-02** | Identity | System must persist users with unique username, email, and one of `STUDENT`, `ORGANIZER`, `FACULTY_ADMIN`. | System |
| **FR-03** | Auth | System must authenticate users via secure BCrypt hash validation and issue role credentials. | Public / All |
| **FR-04** | Venues | System must maintain physical venues with capacity, building location, and technical equipment tags. | Admin / Organizer |
| **FR-05** | Venues | System must provide REST endpoints to create, retrieve, update, and list all active festival venues. | Admin (Write), All (Read) |
| **FR-06** | Categories | System must maintain event categories (Music, Dance, Drama, Tech) with distinct hex color codes. | Admin (Write), All (Read) |
| **FR-07** | Events | System must manage event states: `DRAFT` -> `PENDING_APPROVAL` -> `APPROVED` / `REJECTED` / `CANCELLED`. | Organizer, Admin |
| **FR-08** | Conflict Engine | System must mathematically verify that no two approved events occupy the same venue during overlapping times. | System |
| **FR-09** | Events | System must permit organizers to create and update events, automatically rejecting conflicts with HTTP `409`. | Organizer |
| **FR-10** | Governance | System must allow faculty administrators to review, approve, or reject pending events with audit notes. | Faculty Admin |
| **FR-11** | Discovery | System must expose a public schedule search API filterable by date, venue, category, and status. | Public / All |
| **FR-12** | Registration | System must allow students to register for events while strictly enforcing venue seat capacity limits. | Student |
| **FR-13** | Itinerary | System must provide registered students with a chronological personal itinerary ("My Schedule"). | Student |
| **FR-14** | Dashboard | System must aggregate operational metrics: total events, occupancy by venue, and registration counts. | Organizer, Admin |
| **FR-15** | Observability | System must provide automated health, readiness, and database connectivity diagnostic endpoints. | DevOps / Public |

---

## 3. Non-Functional Requirements (NFR) Specification

```
+-------------------------------------------------------------------------------------------------+
|                               NON-FUNCTIONAL REQUIREMENTS MATRIX                                |
+-------------------------------------------------------------------------------------------------+
| NFR-01: Performance    | P95 API response time < 300 ms; P99 < 500 ms under 500 req/sec load    |
| NFR-02: Concurrency    | Safe transactional concurrency with pessimistic/optimistic locking     |
| NFR-03: Security       | Passwords hashed using BCrypt (strength 12); strict input sanitization |
| NFR-04: Availability   | 99.9% uptime target during active 3-day festival window                |
| NFR-05: Maintainability| Clean 4-tier modular architecture; 80%+ unit/integration test coverage |
| NFR-06: Portability    | Fully containerizable standalone Spring Boot JAR with embedded Tomcat   |
| NFR-07: Data Integrity| ACID-compliant PostgreSQL foreign keys, unique indexes & transactions   |
| NFR-08: Scalability    | Stateless REST architecture capable of horizontal multi-node scaling   |
+-------------------------------------------------------------------------------------------------+
```

---

## 4. Application Architecture

FestPulse implements a **Clean Multi-Tier Layered Architecture**. Each layer maintains strict separation of concerns, decoupling HTTP communication, domain business logic, data persistence, and relational storage.

```
       +-------------------------------------------------------------------+
       |                       PRESENTATION TIER                           |
       |  Web Browser / Mobile Client / REST Consumers (cURL, Postman)     |
       +-------------------------------------------------------------------+
                                         | HTTP / JSON (Port 8080)
       +---------------------------------v---------------------------------+
       |                  EMBEDDED TOMCAT SERVLET CONTAINER                |
       |               Spring Web DispatcherServlet & Filters              |
       +-------------------------------------------------------------------+
                                         |
       +---------------------------------v---------------------------------+
       |                      CONTROLLER / API LAYER                       |
       |  AuthController | EventController | VenueController | HealthController |
       |  - Request Mapping & HTTP Response generation                     |
       |  - DTO Transformation & Jakarta Bean Validation (@Valid)          |
       +-------------------------------------------------------------------+
                                         |
       +---------------------------------v---------------------------------+
       |                    SERVICE / BUSINESS LOGIC LAYER                 |
       |  EventService | ConflictDetectionEngine | VenueService | UserService |
       |  - Transaction boundaries (@Transactional)                        |
       |  - Mathematical overlap detection: max(S1,S2) < min(E1,E2)        |
       |  - Capacity checks and state transitions                          |
       +-------------------------------------------------------------------+
                                         |
       +---------------------------------v---------------------------------+
       |                 DATA ACCESS / REPOSITORY LAYER (JPA)              |
       |  EventRepository | VenueRepository | UserRepository | RegRepository |
       |  - Spring Data JPA abstraction over Hibernate ORM                 |
       |  - Dynamic queries & JPQL conflict detection methods              |
       +-------------------------------------------------------------------+
                                         | HikariCP Connection Pool
       +---------------------------------v---------------------------------+
       |                     PERSISTENCE DATABASE TIER                     |
       |                      PostgreSQL 15+ Database                     |
       |  Tables: users, venues, categories, events, registrations        |
       +-------------------------------------------------------------------+
```

### Layer Descriptions:
1. **Presentation Layer:** Client applications consume stateless JSON endpoints over HTTP.
2. **Embedded Tomcat Container:** Listens on port `8080`, handling incoming thread pools and SSL termination when fronted by an ingress proxy.
3. **Controller Layer:** Maps incoming HTTP verbs (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`) to service methods. Handles DTO deserialization and input validation.
4. **Service Layer:** Encapsulates festival business rules, including the **Conflict Detection Engine**, state transitions, and capacity validations.
5. **Repository Layer:** Extends `JpaRepository` to provide database interactions, derived queries, and transaction management via Hibernate.
6. **Persistence Layer:** PostgreSQL 15+ ensuring relational integrity, foreign key constraints, and transactional consistency.

---

## 5. Domain Data Model & Database Schema

### 5.1 Entity-Relationship (ER) Schema

```
  +----------------------+             +-------------------------+
  |        USERS         |             |         VENUES          |
  +----------------------+             +-------------------------+
  | PK  id               |             | PK  id                  |
  |     username (UQ)    |             |     name (UQ)           |
  |     email (UQ)       |             |     building            |
  |     password_hash    |             |     capacity            |
  |     role (ENUM)      |             |     facilities (TEXT)   |
  |     full_name        |             |     is_active (BOOLEAN) |
  |     department       |             |     created_at          |
  |     created_at       |             +-------------------------+
  +----------------------+                          | 1
             | 1                                    |
             |                                      | has many
             | organizes                            |
             |                                      |
             |           +-----------------------+  |
             +---------->|        EVENTS         |<-+
             |           +-----------------------+
             |           | PK  id                |
             |           | FK  organizer_id      |
             |           | FK  venue_id          |
             |           | FK  category_id       |----+
             |           |     title             |    |
             |           |     description       |    |
             |           |     start_time        |    |
             |           |     end_time          |    | belongs to
             |           |     max_participants  |    |
             |           |     current_count     |    |
             |           |     status (ENUM)     |    |
             |           |     created_at        |    |
             |           +-----------------------+    |
             |                       | 1              |
             |                       |                |
             |                       | has many       v
             | registers             |        +-------------------+
             |                       v        | EVENT_CATEGORIES  |
             |           +--------------------+-------------------+
             |           | EVENT_REGISTRATIONS| PK  id            |
             |           +--------------------+     name (UQ)     |
             +---------->| PK  id             |     description   |
                         | FK  user_id        |     color_code    |
                         | FK  event_id       |     created_at    |
                         |     registered_at  +-------------------+
                         |     status (ENUM)  |
                         +--------------------+
```

### 5.2 Table Definitions & Data Types

#### 1. `users` Table
| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | Primary Key | Unique user identifier |
| `username` | `VARCHAR(50)` | NOT NULL, UNIQUE | University login handle |
| `email` | `VARCHAR(100)` | NOT NULL, UNIQUE | Institutional email |
| `password_hash`| `VARCHAR(255)`| NOT NULL | BCrypt hashed password |
| `role` | `VARCHAR(20)` | NOT NULL | `STUDENT`, `ORGANIZER`, `FACULTY_ADMIN` |
| `full_name` | `VARCHAR(100)` | NOT NULL | User's legal/display name |
| `department` | `VARCHAR(80)` | NULLABLE | Academic department |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Account creation audit |

#### 2. `venues` Table
| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | Primary Key | Unique venue identifier |
| `name` | `VARCHAR(100)` | NOT NULL, UNIQUE | Physical venue name |
| `building` | `VARCHAR(100)` | NOT NULL | Campus zone or building name |
| `capacity` | `INTEGER` | NOT NULL, CHECK (> 0) | Fire-code seating limit |
| `facilities` | `TEXT` | NULLABLE | JSON/Comma list of gear |
| `is_active` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Availability status |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Master record timestamp |

#### 3. `event_categories` Table
| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | Primary Key | Unique category ID |
| `name` | `VARCHAR(50)` | NOT NULL, UNIQUE | Category name (Music, Dance) |
| `description` | `VARCHAR(255)` | NULLABLE | Scope summary |
| `color_code` | `VARCHAR(7)` | NOT NULL, DEFAULT '#3B82F6' | Hex code for calendar UI |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Record creation date |

#### 4. `events` Table
| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | Primary Key | Unique event identifier |
| `title` | `VARCHAR(150)` | NOT NULL | Event name |
| `description` | `TEXT` | NULLABLE | Event rules & description |
| `category_id` | `BIGINT` | NOT NULL, FK -> `event_categories.id` | Category reference |
| `venue_id` | `BIGINT` | NOT NULL, FK -> `venues.id` | Assigned venue |
| `organizer_id` | `BIGINT` | NOT NULL, FK -> `users.id` | Creator/Owner reference |
| `start_time` | `TIMESTAMP` | NOT NULL | Scheduled start timestamp |
| `end_time` | `TIMESTAMP` | NOT NULL, CHECK (end_time > start_time) | Scheduled end timestamp |
| `max_participants`| `INTEGER` | NOT NULL, DEFAULT 100 | Registration capacity cap |
| `current_count`| `INTEGER` | NOT NULL, DEFAULT 0 | Active registered attendees |
| `status` | `VARCHAR(25)` | NOT NULL, DEFAULT 'PENDING_APPROVAL' | `DRAFT`, `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `CANCELLED` |
| `rejection_reason`| `VARCHAR(255)` | NULLABLE | Faculty admin notes |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Audit creation timestamp |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Audit modification timestamp |

*Composite Database Index for Fast Conflict Verification:*  
`CREATE INDEX idx_events_venue_window ON events (venue_id, start_time, end_time) WHERE status = 'APPROVED';`

#### 5. `event_registrations` Table
| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | Primary Key | Registration ID |
| `event_id` | `BIGINT` | NOT NULL, FK -> `events.id` | Target event |
| `user_id` | `BIGINT` | NOT NULL, FK -> `users.id` | Registered student |
| `registered_at`| `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Enrollment timestamp |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT 'CONFIRMED' | `CONFIRMED`, `WAITLISTED`, `CANCELLED` |

*Unique Constraint:* `UNIQUE (event_id, user_id)` (A student cannot register twice for the same event).

---

## 6. Conflict Detection Engine Algorithm

The schedule conflict engine is executed prior to approving or updating any event schedule.

```
Proposed Event:   [======== S_new =============== E_new ========]
Existing Event:            [======== S_exist ============ E_exist =======]

Overlap Condition:
    (S_new < E_exist) AND (E_new > S_exist) AND (Venue_new == Venue_exist)
```

### SQL Conflict Detection Specification:
```sql
SELECT e FROM Event e 
WHERE e.venue.id = :venueId 
  AND e.status = 'APPROVED'
  AND (:eventId IS NULL OR e.id != :eventId)
  AND (e.startTime < :newEndTime AND e.endTime > :newStartTime)
```
If this query returns any record, a `ScheduleConflictException` is raised, and the API returns HTTP `409 Conflict` with the offending event details.

---

## 7. REST API Interface Specification (MVP Endpoints)

All endpoints accept and produce `application/json;charset=UTF-8`. Base Path: `/api/v1`.

### 7.1 Authentication & User Management
| Verb | Endpoint | Access Role | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register new user account | `201 Created` | `400`, `409 Conflict` |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user credentials | `200 OK` | `400`, `401 Unauthorized` |
| `GET` | `/api/v1/auth/me` | Authenticated | Retrieve current user profile | `200 OK` | `401 Unauthorized` |

### 7.2 Venue Management
| Verb | Endpoint | Access Role | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/venues` | All | List all active campus venues | `200 OK` | `500` |
| `POST` | `/api/v1/venues` | Faculty Admin | Create new physical venue | `201 Created` | `400`, `409 Conflict` |
| `GET` | `/api/v1/venues/{id}` | All | Get venue details by ID | `200 OK` | `404 Not Found` |
| `PUT` | `/api/v1/venues/{id}` | Faculty Admin | Update venue capacity/facilities | `200 OK` | `400`, `404 Not Found` |

### 7.3 Category Management
| Verb | Endpoint | Access Role | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/categories` | All | List all event categories | `200 OK` | `500` |
| `POST` | `/api/v1/categories` | Faculty Admin | Create new event category | `201 Created` | `400`, `409 Conflict` |

### 7.4 Event Scheduling & Workflow
| Verb | Endpoint | Access Role | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/events` | All | Query festival schedule (filters: date, venue, category, status) | `200 OK` | `400` |
| `GET` | `/api/v1/events/{id}`| All | Retrieve full event details | `200 OK` | `404 Not Found` |
| `POST` | `/api/v1/events` | Organizer / Admin | Propose/Schedule event (runs conflict check) | `201 Created` | `400`, `409 Conflict` |
| `PUT` | `/api/v1/events/{id}`| Organizer / Admin | Edit event details or timing | `200 OK` | `400`, `409 Conflict`, `404` |
| `DELETE`| `/api/v1/events/{id}`| Organizer / Admin | Cancel/Delete festival event | `204 No Content`| `404 Not Found` |
| `PATCH`| `/api/v1/events/{id}/status`| Faculty Admin | Approve or Reject event proposal | `200 OK` | `400`, `404`, `409 Conflict` |

### 7.5 Student Registrations & Itinerary
| Verb | Endpoint | Access Role | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/events/{id}/register` | Student | Register for event (capacity checked) | `201 Created` | `404`, `409`, `422 Unprocessable` |
| `DELETE`| `/api/v1/events/{id}/register` | Student | Cancel student registration | `204 No Content`| `404 Not Found` |
| `GET` | `/api/v1/users/me/schedule` | Student | Fetch student's personal itinerary | `200 OK` | `401 Unauthorized` |

### 7.6 Dashboard & Observability
| Verb | Endpoint | Access Role | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/dashboard/stats` | Organizer / Admin | Fetch operational metrics & counts | `200 OK` | `500` |
| `GET` | `/api/v1/dashboard/conflicts`| Organizer / Admin | Inspect schedule clash reports | `200 OK` | `500` |
| `GET` | `/api/v1/health` | Public | System and DB connectivity probe | `200 OK` | `503 Service Unavailable` |
