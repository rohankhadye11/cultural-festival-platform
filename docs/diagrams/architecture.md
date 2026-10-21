# Application Architecture & Component Diagram

This document illustrates the technical component architecture, data flow, and runtime deployment model for the **Automated Cultural Festival Schedule Platform (FestPulse)**.

---

## 1. High-Level Multi-Tier Architecture Diagram

```mermaid
graph TD
    %% Clients
    subgraph ClientTier ["Client Tier (Presentation)"]
        WEB["Desktop Web Browser<br/>(Chrome / Firefox / Edge)"]
        MOBILE["Mobile Device Browser<br/>(iOS / Android Responsive UI)"]
        API_CLIENT["REST API Consumer<br/>(cURL / Postman / Automated Tests)"]
    end

    %% Web & Application Tier
    subgraph AppTier ["Application Tier (Spring Boot 3.x & Embedded Tomcat :8080)"]
        
        subgraph Container ["Embedded Apache Tomcat 10.x"]
            DISPATCHER["Spring DispatcherServlet<br/>HTTP Request Router"]
            FILTERS["Servlet Filters<br/>CORS, Request Logging, Security"]
        end

        subgraph Controllers ["Controller / REST API Layer"]
            AUTH_C["AuthController<br/>/api/v1/auth/*"]
            EVENT_C["EventController<br/>/api/v1/events/*"]
            VENUE_C["VenueController<br/>/api/v1/venues/*"]
            DASH_C["DashboardController<br/>/api/v1/dashboard/*"]
            HEALTH_C["HealthController<br/>/api/v1/health"]
        end

        subgraph Services ["Service / Business Domain Layer"]
            AUTH_S["UserService & Auth<br/>BCrypt Hashing & RBAC"]
            EVENT_S["EventService<br/>Lifecycle State Machine"]
            CONFLICT_E["ConflictDetectionEngine<br/>Temporal-Spatial Math Check"]
            VENUE_S["VenueService<br/>Capacity & Resource Rules"]
            REG_S["RegistrationService<br/>Seat Capping & Itinerary"]
            DASH_S["DashboardService<br/>Operational Metrics Aggregation"]
        end

        subgraph Repositories ["Data Access Layer (Spring Data JPA / Hibernate)"]
            USER_R["UserRepository"]
            EVENT_R["EventRepository"]
            VENUE_R["VenueRepository"]
            CAT_R["CategoryRepository"]
            REG_R["RegistrationRepository"]
        end

        subgraph CrossCutting ["Cross-Cutting Concerns"]
            VALIDATION["Jakarta Validation (@Valid)<br/>Field & Constraint Checks"]
            EXCEPTION["GlobalExceptionHandler<br/>(@RestControllerAdvice)"]
            ACTUATOR["Spring Boot Actuator<br/>Health, Metrics & Info"]
        end

    end

    %% Database Tier
    subgraph DataTier ["Persistence Tier (PostgreSQL 15+)"]
        HIKARI["HikariCP Connection Pool<br/>Maximum Connections: 10"]
        
        subgraph PostgresTables ["Relational Schema (festival_db)"]
            T_USERS["users"]
            T_VENUES["venues"]
            T_CATS["event_categories"]
            T_EVENTS["events<br/>(idx_events_venue_window)"]
            T_REGS["event_registrations"]
        end
    end

    %% Relationships & Data Flow
    WEB -->|HTTP JSON / Port 8080| FILTERS
    MOBILE -->|HTTP JSON / Port 8080| FILTERS
    API_CLIENT -->|HTTP JSON / Port 8080| FILTERS

    FILTERS --> DISPATCHER
    DISPATCHER --> Controllers
    Controllers -.-> VALIDATION
    Controllers -.-> EXCEPTION

    AUTH_C --> AUTH_S
    EVENT_C --> EVENT_S
    VENUE_C --> VENUE_S
    DASH_C --> DASH_S
    HEALTH_C --> ACTUATOR

    EVENT_S --> CONFLICT_E
    EVENT_S --> REG_S

    AUTH_S --> USER_R
    EVENT_S --> EVENT_R
    VENUE_S --> VENUE_R
    REG_S --> REG_R
    DASH_S --> EVENT_R
    DASH_S --> VENUE_R

    Repositories --> HIKARI
    HIKARI --> PostgresTables

    style ClientTier fill:#e2e8f0,stroke:#475569,stroke-width:2px;
    style AppTier fill:#f8fafc,stroke:#1e293b,stroke-width:2px;
    style DataTier fill:#e0f2fe,stroke:#0369a1,stroke-width:2px;
    style Container fill:#fef3c7,stroke:#d97706,stroke-width:1px;
    style Controllers fill:#dcfce7,stroke:#16a34a,stroke-width:1px;
    style Services fill:#ede9fe,stroke:#7c3aed,stroke-width:1px;
    style Repositories fill:#fed7aa,stroke:#ea580c,stroke-width:1px;
```

---

## 2. Component Interaction & Request Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Organizer as Event Organizer
    participant Tomcat as Embedded Tomcat (:8080)
    participant Ctrl as EventController
    participant Svc as EventService
    participant Engine as ConflictDetectionEngine
    participant Repo as EventRepository
    participant DB as PostgreSQL (festival_db)

    Organizer->>Tomcat: POST /api/v1/events (JSON Payload)
    Tomcat->>Ctrl: Dispatch to createEvent(@Valid EventRequestDTO)
    Ctrl->>Ctrl: Validate Request (Jakarta @NotNull, @Future)
    Ctrl->>Svc: scheduleEvent(dto)
    
    Svc->>Engine: checkConflict(venueId, startTime, endTime)
    Engine->>Repo: findOverlappingEvents(venueId, start, end)
    Repo->>DB: SELECT * FROM events WHERE venue_id=? AND (overlap condition)
    DB-->>Repo: Return matched records (Empty set)
    Repo-->>Engine: 0 overlapping events found
    Engine-->>Svc: Conflict Check Passed (OK)
    
    Svc->>Repo: save(EventEntity[status=PENDING_APPROVAL])
    Repo->>DB: INSERT INTO events (...) VALUES (...)
    DB-->>Repo: Saved entity with generated PK ID
    Repo-->>Svc: Persisted Event
    Svc-->>Ctrl: EventResponseDTO
    Ctrl-->>Tomcat: HTTP 201 Created (JSON Response)
    Tomcat-->>Organizer: Return 201 Created + Event Payload
```
