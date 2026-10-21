package com.festival.schedule.controller;

import com.festival.schedule.model.Venue;
import com.festival.schedule.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing complete CRUD endpoints for Venue management.
 * Base Path: /api/v1/venues
 */
@RestController
@RequestMapping("/api/v1/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * Create a new physical campus venue.
     * POST /api/v1/venues
     */
    @PostMapping
    public ResponseEntity<Venue> createVenue(@Valid @RequestBody Venue venue) {
        Venue created = venueService.createVenue(venue);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List all campus venues, optionally filtering by active status.
     * GET /api/v1/venues?activeOnly=true
     */
    @GetMapping
    public ResponseEntity<List<Venue>> getAllVenues(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<Venue> venues = activeOnly ? venueService.getActiveVenues() : venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    /**
     * Retrieve a specific venue by ID.
     * GET /api/v1/venues/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long id) {
        Venue venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    /**
     * Update an existing venue's properties or capacity.
     * PUT /api/v1/venues/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Venue> updateVenue(
            @PathVariable Long id,
            @RequestBody Venue updatedVenue
    ) {
        Venue saved = venueService.updateVenue(id, updatedVenue);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete a venue by ID.
     * DELETE /api/v1/venues/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
