# 01. Problem Scope & MVP Definition

**Project:** Automated Cultural Festival Schedule Platform  
**System Codename:** FestPulse  
**Domain:** Higher Education / Campus Event & Resource Operations  
**Document Version:** 1.0.0-RELEASE  
**Date:** October 2026  
**Status:** Approved for Phase 1 Scaffolding & MVP Development  

---

## 1. Executive Summary

Annual university cultural festivals represent high-stakes, multi-day, campus-wide operations involving thousands of student attendees, hundreds of competitive and non-competitive events, and dozens of physical facilities. Currently, university organizing committees manage schedule planning, venue bookings, stage setups, and participant communications using a fragmented patchwork of spreadsheets, instant messaging channels (e.g., WhatsApp groups), physical noticeboards, and ad-hoc email chains.

This operational model suffers from critical points of failure: accidental double-booking of premier auditoriums, desynchronized schedule updates when performances run overtime, delayed incident communications during inclement weather, and registration bottlenecks that overwhelm coordinators.

The **Automated Cultural Festival Schedule Platform (FestPulse)** is an enterprise-grade, web-based schedule automation and resource coordination system. Built with **Java 21**, **Spring Boot**, **PostgreSQL**, and deployed on an **embedded Tomcat servlet container**, this platform enforces centralized governance over event scheduling, automates physical venue conflict resolution, supports role-based access control (RBAC), and delivers real-time schedule awareness to students, faculty advisors, and student organizers.

---

## 2. Target Users & User Personas

| Persona Role | Target Audience | Primary Goals | Core Frustrations with Existing Process |
| :--- | :--- | :--- | :--- |
| **Student Attendee / Participant** | Undergraduate and graduate students, collegiate performers, visiting inter-college teams. | Browse consolidated fest schedule; filter events by genre/time; register for competitions; receive timely notifications for schedule/venue shifts. | Missed performances due to outdated PDF timetables; conflicting event timings; no central place to view personal registered events. |
| **Student Event Organizer** | Cultural committee heads, sub-event leads (dance, drama, music, literary, tech-arts). | Publish event details; allocate stages and technical equipment; manage participant rosters; update time slots dynamically. | Accidental clash with other clubs for the same venue; chaotic manual attendance tracking; endless WhatsApp queries from participants. |
| **Faculty Advisor / Administrator** | Dean of Student Affairs, Faculty Event Coordinators, Campus Security, Facility Heads. | Review and approve event proposals; supervise venue capacity and safety limits; verify budget and equipment allocation; inspect audit logs. | Lack of formal sign-off hierarchy; unawareness of last-minute schedule alterations; inability to assess real-time crowd distribution across campus. |
| **Facility & Logistics Manager** | Campus infrastructure crew, sound/lighting technicians, auditorium stage managers. | Receive advance equipment and stage setup requisitions; coordinate turnaround time between back-to-back performances. | Insufficient buffer time between heavy theatrical/band setups; unnotified venue changes leading to misplaced equipment. |

---

## 3. Pain Points of Manual Scheduling

```
           +-------------------------------------------------------------+
           |               PAIN POINTS OF MANUAL SCHEDULING              |
           +-------------------------------------------------------------+
                                          |
        +------------------+--------------+------------------+
        |                  |                                 |
+-------v-------+  +-------v--------+  +---------------------+  +--------v-------+
| Venue & Time  |  | Communication  |  | Registration        |  | Zero Audit &   |
| Clashes       |  | Breakdown      |  | Bottlenecks         |  | Analytics      |
|               |  |                |  |                     |  |                |
| Spreadsheets  |  | Static PDFs,   |  | Paper sign-ups,     |  | No tracking of |
| lack mutual   |  | stale bulletin |  | capacity overruns,  |  | room usage,    |
| exclusion     |  | boards, group  |  | no waitlist         |  | event trends,  |
| validation.   |  | chat clutter.  |  | management.         |  | or changes.    |
+---------------+  +----------------+  +---------------------+  +----------------+
```

### 3.1 Venue and Time Collisions
Physical facilities (main auditorium, open-air amphitheater, seminar halls, dance studios) are shared resources. In a spreadsheet-driven workflow, two independent organizers can simultaneously mark the amphitheater for 3:00 PM without automated validation, resulting in public collisions and delayed performances on festival days.

### 3.2 Information Desynchronization & Latency
When an acoustic music performance exceeds its allocated slot by 35 minutes, downstream events are delayed. Without an automated platform, organizers rely on manual word-of-mouth or social media announcements. Attendees arrive at empty or occupied venues, causing widespread confusion and frustration.

### 3.3 Manual Registration and Over-Capacity Risks
Events hosted in indoor halls have strict fire-code capacity caps. Manual Google Forms or paper registrations fail to lock registrations once room limits are reached, resulting in overcrowding, security interventions, and disqualified participants.

### 3.4 Absence of Role-Based Governance
In open spreadsheets, any volunteer with edit access can inadvertently alter or overwrite another committee's time slot, delete participant records, or change event rules without accountability.

### 3.5 Lack of Operational Metrics
Post-festival reports require weeks of manual data compilation. Faculty administrators cannot review attendance trends, venue utilization efficiency, or peak congestion periods to optimize future resource allocation.

---

## 4. Stakeholder Analysis Matrix

| Stakeholder Group | Interest / Mission | Impact Level | Influence Level | Key Expectations |
| :--- | :--- | :--- | :--- | :--- |
| **Dean of Student Affairs / Faculty Board** | Governance, safety, regulatory compliance, university prestige. | High | High | Strict approval gates, tamper-proof audit trails, safety compliance. |
| **Core Student Cultural Council** | Seamless execution, high attendee satisfaction, schedule adherence. | High | High | Automated conflict detection, intuitive dashboard, fast editing workflows. |
| **Event Lead Organizers (Clubs/Societies)** | Smooth execution of specific events (e.g., Battle of the Bands). | High | Medium | Self-service event creation, participant capacity caps, live status updates. |
| **General Student Population & Performers** | Enjoyment, participation, friction-free discovery. | High | Low | Responsive mobile-friendly UI, fast search, personalized schedule bookmarker. |
| **Campus Security & Facilities Administration** | Crowd management, fire safety, equipment preservation. | Medium | High | Room capacity caps, accurate venue occupancy schedules, emergency broadcasts. |

---

## 5. System Constraints

### 5.1 Technical Constraints
- **Backend Stack:** Java 21 (LTS), Spring Boot 3.3.x, embedded Apache Tomcat 10.x.
- **Persistence Layer:** PostgreSQL 15+ relational database accessed via Spring Data JPA and Hibernate ORM with HikariCP connection pooling.
- **Architecture:** Monolithic, clean-layered RESTful service with stateless session mechanics ready for containerized execution.
- **Port Assignment:** Default HTTP port fixed at `8080`.

### 5.2 Operational & Environmental Constraints
- **Peak Concurrency:** Expected load spike during the 3 days of the festival with up to 1,000 concurrent active users querying schedules.
- **Network Variability:** University Wi-Fi networks experience high packet loss during large gatherings; payloads must remain lean and compressed.
- **Resource Footprint:** Capable of running inside a single containerized node with 2 vCPUs and 2 GB RAM.

### 5.3 Security & Compliance Constraints
- **Authentication & RBAC:** Enforced Role-Based Access Control dividing privileges across `STUDENT`, `ORGANIZER`, and `FACULTY_ADMIN`.
- **Data Protection:** Passwords securely hashed using BCrypt. Student PII restricted to registered festival events.
- **Auditability:** Immutable timestamps on all event modifications, approval status changes, and venue allocations.

---

## 6. Success Criteria & Key Performance Indicators (KPIs)

| KPI Metric | Baseline (Manual Process) | Target (FestPulse Platform) | Measurement Method |
| :--- | :--- | :--- | :--- |
| **Venue Scheduling Clashes** | 12 to 18 clashes per festival | **0 clashes (100% eliminated)** | Database constraint validation & conflict detection engine. |
| **Schedule Update Latency** | 45 - 90 minutes across campus | **< 3 seconds** | Time from organizer update to public API reflection. |
| **Event Query API Response Time** | N/A (Static files) | **< 300 ms (P95 under 500 req/s)** | JMeter / Spring Boot Actuator metrics. |
| **Venue Capacity Violations** | Frequent (uncontrolled entry) | **0 violations** | Automated registration threshold hard-capping. |
| **Student Adoption Rate** | Fragmented across apps | **> 85% of registered students** | Active user registrations vs. total campus enrollment. |

---

## 7. 15-Task MVP Scope Freeze

To ensure on-time delivery for Phase 1 and maintain rigorous engineering quality, the platform scope is strictly bounded to the following **15-Task MVP Scope Freeze**. Features outside this matrix (e.g., automated AI schedule optimization, third-party payment gateways, QR-code turnstiles) are deferred to Phase 2.

```
+-----------------------------------------------------------------------------------------+
|                               15-TASK MVP SCOPE FREEZE                                  |
+-----------------------------------------------------------------------------------------+
| MODULE A: Core Foundation, Identity & RBAC                                              |
|   Task 01: Scaffold Spring Boot 3.x project with Maven, PostgreSQL & Embedded Tomcat    |
|   Task 02: Implement User entity, Roles (STUDENT, ORGANIZER, ADMIN) & Repository        |
|   Task 03: Build Authentication & Registration REST Endpoints with BCrypt Security      |
+-----------------------------------------------------------------------------------------+
| MODULE B: Venue & Category Resource Management                                          |
|   Task 04: Implement Physical Venue Master Data Model & CRUD Repositories               |
|   Task 05: Build Venue Management REST API with Capacity & Facilities Metadata          |
|   Task 06: Implement Event Category Master Model & REST Endpoints (Music, Dance, etc.)  |
+-----------------------------------------------------------------------------------------+
| MODULE C: Event Scheduling & Conflict Engine                                            |
|   Task 07: Implement Event Entity with Lifecycle State Machine (DRAFT, APPROVED, etc.)  |
|   Task 08: Develop Core Schedule Conflict Engine (Temporal + Spatial Overlap Detection) |
|   Task 09: Build Event Creation & Updation REST Endpoints with Auto-Conflict Rejection  |
|   Task 10: Build Faculty Admin Approval / Rejection Workflow REST Endpoints             |
+-----------------------------------------------------------------------------------------+
| MODULE D: Student Registration & Public Discovery                                       |
|   Task 11: Implement Public Filterable Festival Schedule Feed API (Date, Venue, Genre)  |
|   Task 12: Build Student Event Registration Workflow with Capacity Hard-Capping         |
|   Task 13: Build "My Itinerary" Personal Schedule API for Registered Students           |
+-----------------------------------------------------------------------------------------+
| MODULE E: Operations Dashboard & System Observability                                   |
|   Task 14: Build Central Operational Metrics Dashboard API (Venue Load, Registrations)  |
|   Task 15: Implement Health Check, DB Connectivity Verification & Actuator Metrics      |
+-----------------------------------------------------------------------------------------+
```

### Out-of-Scope for Phase 1 MVP (Post-MVP Backlog)
1. Payment gateway integration for ticketed celebrity nights.
2. Bluetooth beacon / GPS-based indoor campus navigation.
3. Automated multi-variable genetic algorithm for timetable generation.
4. SMS gateway integrations (replaced by REST polling & notifications in Phase 1).
