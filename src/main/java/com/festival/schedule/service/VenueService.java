package com.festival.schedule.service;

import com.festival.schedule.exception.ResourceNotFoundException;
import com.festival.schedule.model.Venue;
import com.festival.schedule.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing physical campus venues, stage capacities, and facility allocations.
 */
@Service
public class VenueService {

    private static final Logger logger = LoggerFactory.getLogger(VenueService.class);

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    /**
     * Create a new campus venue.
     */
    @Transactional
    public Venue createVenue(Venue venue) {
        if (venue.getName() == null || venue.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue name must not be blank");
        }
        if (venue.getCapacity() == null || venue.getCapacity() <= 0) {
            throw new IllegalArgumentException("Venue capacity must be greater than zero");
        }
        if (venueRepository.existsByNameIgnoreCase(venue.getName().trim())) {
            throw new IllegalArgumentException("Venue with name '" + venue.getName() + "' already exists");
        }

        venue.setName(venue.getName().trim());
        if (venue.getIsActive() == null) {
            venue.setIsActive(true);
        }

        Venue saved = venueRepository.save(venue);
        logger.info("Created venue: '{}' (ID: {}, Capacity: {})", saved.getName(), saved.getId(), saved.getCapacity());
        return saved;
    }

    /**
     * Retrieve all venues.
     */
    @Transactional(readOnly = true)
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    /**
     * Retrieve all active venues.
     */
    @Transactional(readOnly = true)
    public List<Venue> getActiveVenues() {
        return venueRepository.findByIsActiveTrue();
    }

    /**
     * Retrieve a single venue by ID or throw ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }

    /**
     * Update an existing venue by ID.
     */
    @Transactional
    public Venue updateVenue(Long id, Venue updatedVenue) {
        Venue existing = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));

        if (updatedVenue.getName() != null && !updatedVenue.getName().trim().isEmpty()) {
            String trimmedName = updatedVenue.getName().trim();
            if (venueRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
                throw new IllegalArgumentException("Another venue with name '" + trimmedName + "' already exists");
            }
            existing.setName(trimmedName);
        }

        if (updatedVenue.getBuilding() != null && !updatedVenue.getBuilding().trim().isEmpty()) {
            existing.setBuilding(updatedVenue.getBuilding().trim());
        }

        if (updatedVenue.getCapacity() != null) {
            if (updatedVenue.getCapacity() <= 0) {
                throw new IllegalArgumentException("Venue capacity must be greater than zero");
            }
            existing.setCapacity(updatedVenue.getCapacity());
        }

        if (updatedVenue.getFacilities() != null) {
            existing.setFacilities(updatedVenue.getFacilities());
        }

        if (updatedVenue.getIsActive() != null) {
            existing.setIsActive(updatedVenue.getIsActive());
        }

        Venue saved = venueRepository.save(existing);
        logger.info("Updated venue ID {}: '{}'", id, saved.getName());
        return saved;
    }

    /**
     * Delete a venue by ID.
     */
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));

        venueRepository.delete(venue);
        logger.info("Deleted venue ID {}: '{}'", id, venue.getName());
    }
}
