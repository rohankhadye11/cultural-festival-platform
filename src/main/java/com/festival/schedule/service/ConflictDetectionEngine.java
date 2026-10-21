package com.festival.schedule.service;

import com.festival.schedule.exception.ScheduleConflictException;
import com.festival.schedule.model.Event;
import com.festival.schedule.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Core mathematical engine validating that events do not collide in time and physical space.
 * 
 * Formal Overlap Condition:
 * An overlap between an existing Event A [S_A, E_A] and a proposed Event B [S_B, E_B] at Venue V occurs if:
 *   max(S_A, S_B) < min(E_A, E_B)
 * Which equivalently reduces in SQL to:
 *   (S_A < E_B) AND (E_A > S_B)
 */
@Service
public class ConflictDetectionEngine {

    private static final Logger logger = LoggerFactory.getLogger(ConflictDetectionEngine.class);

    private final EventRepository eventRepository;

    public ConflictDetectionEngine(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Validates that the requested venue and time window have no collisions with approved events.
     * Throws ScheduleConflictException if a clash is detected.
     *
     * @param venueId Target venue ID
     * @param startTime Proposed event start time
     * @param endTime Proposed event end time
     * @param excludeEventId Optional event ID to exclude (used when rescheduling/updating an event)
     * @throws ScheduleConflictException when a collision occurs
     * @throws IllegalArgumentException when inputs are null or end time is before start time
     */
    @Transactional(readOnly = true)
    public void validateNoConflict(Long venueId, LocalDateTime startTime, LocalDateTime endTime, Long excludeEventId) {
        List<Event> conflicts = findConflicts(venueId, startTime, endTime, excludeEventId);
        if (!conflicts.isEmpty()) {
            Event firstConflict = conflicts.get(0);
            String message = String.format(
                    "Schedule collision at venue ID %d: Conflicting approved event '%s' (ID: %d) is scheduled from %s to %s.",
                    venueId,
                    firstConflict.getTitle(),
                    firstConflict.getId(),
                    firstConflict.getStartTime(),
                    firstConflict.getEndTime()
            );
            logger.warn(message);
            throw new ScheduleConflictException(message, venueId, firstConflict.getId());
        }
    }

    /**
     * Finds all approved events at the specified venue that overlap with the target time slot.
     */
    @Transactional(readOnly = true)
    public List<Event> findConflicts(Long venueId, LocalDateTime startTime, LocalDateTime endTime, Long excludeEventId) {
        if (venueId == null) {
            throw new IllegalArgumentException("Venue ID cannot be null for conflict validation");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be strictly after start time");
        }

        return eventRepository.findConflictingApprovedEvents(venueId, startTime, endTime, excludeEventId);
    }

    /**
     * Boolean check indicating whether a conflict exists.
     */
    @Transactional(readOnly = true)
    public boolean hasConflict(Long venueId, LocalDateTime startTime, LocalDateTime endTime, Long excludeEventId) {
        return !findConflicts(venueId, startTime, endTime, excludeEventId).isEmpty();
    }
}
