package com.festival.schedule.service;

import com.festival.schedule.exception.ResourceNotFoundException;
import com.festival.schedule.model.Event;
import com.festival.schedule.model.EventCategory;
import com.festival.schedule.model.EventStatus;
import com.festival.schedule.model.User;
import com.festival.schedule.model.Venue;
import com.festival.schedule.repository.EventCategoryRepository;
import com.festival.schedule.repository.EventRepository;
import com.festival.schedule.repository.UserRepository;
import com.festival.schedule.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing festival event lifecycle, booking workflows, conflict validation, and summary metrics.
 */
@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final EventCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ConflictDetectionEngine conflictEngine;

    public EventService(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            EventCategoryRepository categoryRepository,
            UserRepository userRepository,
            ConflictDetectionEngine conflictEngine
    ) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.conflictEngine = conflictEngine;
    }

    /**
     * Create / schedule a new festival event with automated conflict detection.
     */
    @Transactional
    public Event createEvent(Event event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title must not be blank");
        }
        if (event.getStartTime() == null || event.getEndTime() == null) {
            throw new IllegalArgumentException("Event start time and end time are required");
        }
        if (!event.getEndTime().isAfter(event.getStartTime())) {
            throw new IllegalArgumentException("Event end time must be strictly after start time");
        }
        if (event.getVenue() == null || event.getVenue().getId() == null) {
            throw new IllegalArgumentException("Valid venue with an ID must be assigned to the event");
        }

        // Validate that the assigned venue exists
        Long venueId = event.getVenue().getId();
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", venueId));
        event.setVenue(venue);

        // Validate Category if specified
        if (event.getCategory() != null && event.getCategory().getId() != null) {
            EventCategory category = categoryRepository.findById(event.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventCategory", event.getCategory().getId()));
            event.setCategory(category);
        }

        // Validate Organizer if specified
        if (event.getOrganizer() != null && event.getOrganizer().getId() != null) {
            User organizer = userRepository.findById(event.getOrganizer().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User (Organizer)", event.getOrganizer().getId()));
            event.setOrganizer(organizer);
        }

        // Set default status if not set
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.PENDING_APPROVAL);
        }

        // Check for conflicts against existing APPROVED events at this venue
        conflictEngine.validateNoConflict(venueId, event.getStartTime(), event.getEndTime(), null);

        Event saved = eventRepository.save(event);
        logger.info("Successfully scheduled event '{}' (ID: {}) at venue '{}' for {}.",
                saved.getTitle(), saved.getId(), venue.getName(), saved.getStartTime());
        return saved;
    }

    /**
     * Retrieve all approved events (default public schedule view).
     */
    @Transactional(readOnly = true)
    public List<Event> getApprovedEvents() {
        return eventRepository.findByStatusOrderByStartTimeAsc(EventStatus.APPROVED);
    }

    /**
     * Retrieve all events without filtering.
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Retrieve a specific event by ID.
     */
    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    /**
     * Query events with dynamic multi-criteria filters.
     */
    @Transactional(readOnly = true)
    public List<Event> filterEvents(EventStatus status, Long categoryId, Long venueId, LocalDateTime fromTime, LocalDateTime toTime) {
        return eventRepository.filterEvents(status, categoryId, venueId, fromTime, toTime);
    }

    /**
     * Update an existing event with re-verification of conflict safety.
     */
    @Transactional
    public Event updateEvent(Long id, Event updatedEvent) {
        Event existing = getEventById(id);

        if (updatedEvent.getTitle() != null && !updatedEvent.getTitle().trim().isEmpty()) {
            existing.setTitle(updatedEvent.getTitle().trim());
        }
        if (updatedEvent.getDescription() != null) {
            existing.setDescription(updatedEvent.getDescription());
        }
        if (updatedEvent.getMaxParticipants() != null) {
            if (updatedEvent.getMaxParticipants() <= 0) {
                throw new IllegalArgumentException("Max participants must be greater than zero");
            }
            existing.setMaxParticipants(updatedEvent.getMaxParticipants());
        }

        // If timing or venue changes, validate conflict
        Long targetVenueId = updatedEvent.getVenue() != null && updatedEvent.getVenue().getId() != null
                ? updatedEvent.getVenue().getId()
                : existing.getVenue().getId();

        LocalDateTime targetStart = updatedEvent.getStartTime() != null ? updatedEvent.getStartTime() : existing.getStartTime();
        LocalDateTime targetEnd = updatedEvent.getEndTime() != null ? updatedEvent.getEndTime() : existing.getEndTime();

        if (!targetEnd.isAfter(targetStart)) {
            throw new IllegalArgumentException("Event end time must be strictly after start time");
        }

        // Run conflict validation excluding this event itself
        conflictEngine.validateNoConflict(targetVenueId, targetStart, targetEnd, id);

        if (updatedEvent.getVenue() != null && updatedEvent.getVenue().getId() != null) {
            Venue venue = venueRepository.findById(targetVenueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venue", targetVenueId));
            existing.setVenue(venue);
        }

        existing.setStartTime(targetStart);
        existing.setEndTime(targetEnd);

        Event saved = eventRepository.save(existing);
        logger.info("Updated event ID {}: '{}'", id, saved.getTitle());
        return saved;
    }

    /**
     * Approve a pending event, validating that no clash was introduced in the interim.
     */
    @Transactional
    public Event approveEvent(Long id) {
        Event event = getEventById(id);

        // Verify conflict before approval
        conflictEngine.validateNoConflict(
                event.getVenue().getId(),
                event.getStartTime(),
                event.getEndTime(),
                event.getId()
        );

        event.setStatus(EventStatus.APPROVED);
        event.setRejectionReason(null);
        Event saved = eventRepository.save(event);
        logger.info("Event ID {} ('{}') has been APPROVED.", id, saved.getTitle());
        return saved;
    }

    /**
     * Reject an event with explanation notes.
     */
    @Transactional
    public Event rejectEvent(Long id, String reason) {
        Event event = getEventById(id);
        event.setStatus(EventStatus.REJECTED);
        event.setRejectionReason(reason);
        Event saved = eventRepository.save(event);
        logger.info("Event ID {} ('{}') has been REJECTED. Reason: {}", id, saved.getTitle(), reason);
        return saved;
    }

    /**
     * Cancel an event.
     */
    @Transactional
    public Event cancelEvent(Long id) {
        Event event = getEventById(id);
        event.setStatus(EventStatus.CANCELLED);
        Event saved = eventRepository.save(event);
        logger.info("Event ID {} ('{}') has been CANCELLED.", id, saved.getTitle());
        return saved;
    }

    /**
     * Aggregates summary statistics for the operations dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        long totalEvents = eventRepository.count();
        long approved = eventRepository.countByStatus(EventStatus.APPROVED);
        long pending = eventRepository.countByStatus(EventStatus.PENDING_APPROVAL);
        long draft = eventRepository.countByStatus(EventStatus.DRAFT);
        long rejected = eventRepository.countByStatus(EventStatus.REJECTED);
        long cancelled = eventRepository.countByStatus(EventStatus.CANCELLED);

        summary.put("totalEvents", totalEvents);
        summary.put("statusBreakdown", Map.of(
                "APPROVED", approved,
                "PENDING_APPROVAL", pending,
                "DRAFT", draft,
                "REJECTED", rejected,
                "CANCELLED", cancelled
        ));
        summary.put("totalApproved", approved);
        summary.put("totalPendingApproval", pending);
        summary.put("totalRejected", rejected);
        summary.put("totalVenuesConfigured", venueRepository.count());
        summary.put("activeVenuesCount", venueRepository.findByIsActiveTrue().size());

        return summary;
    }
}
