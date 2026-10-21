package com.festival.schedule.controller;

import com.festival.schedule.model.Event;
import com.festival.schedule.model.EventStatus;
import com.festival.schedule.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing Event Scheduling, Filtering, Lifecycle Actions, and Summary Dashboard endpoints.
 * Base Path: /api/v1/events
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Create and schedule a new festival event.
     * Enforces automated conflict detection before saving.
     * POST /api/v1/events
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event created = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Browse festival events with optional multi-criteria filters.
     * GET /api/v1/events?status=APPROVED&venueId=1&categoryId=2
     */
    @GetMapping
    public ResponseEntity<List<Event>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (status != null || categoryId != null || venueId != null || from != null || to != null) {
            return ResponseEntity.ok(eventService.filterEvents(status, categoryId, venueId, from, to));
        }
        return ResponseEntity.ok(eventService.getApprovedEvents());
    }

    /**
     * Summary Dashboard endpoint aggregating total events per status, venue usage, and festival metrics.
     * GET /api/v1/events/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryDashboard() {
        Map<String, Object> summary = eventService.getEventSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Retrieve a specific event by ID.
     * GET /api/v1/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    /**
     * Update an event's details or time slot (re-validates conflict safety).
     * PUT /api/v1/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody Event updatedEvent
    ) {
        Event saved = eventService.updateEvent(id, updatedEvent);
        return ResponseEntity.ok(saved);
    }

    /**
     * Faculty Admin action: Approve an event and publish to schedule.
     * PATCH /api/v1/events/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Event> approveEvent(@PathVariable Long id) {
        Event approved = eventService.approveEvent(id);
        return ResponseEntity.ok(approved);
    }

    /**
     * Faculty Admin action: Reject a pending event.
     * PATCH /api/v1/events/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Event> rejectEvent(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Schedule or administrative conflict") String reason
    ) {
        Event rejected = eventService.rejectEvent(id, reason);
        return ResponseEntity.ok(rejected);
    }

    /**
     * Cancel an event.
     * PATCH /api/v1/events/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Event> cancelEvent(@PathVariable Long id) {
        Event cancelled = eventService.cancelEvent(id);
        return ResponseEntity.ok(cancelled);
    }
}
