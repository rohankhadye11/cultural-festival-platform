package com.festival.schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a proposed event schedule collides with an existing approved booking at the same venue.
 * Automatically maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleConflictException extends RuntimeException {

    private final Long venueId;
    private final Long conflictingEventId;

    public ScheduleConflictException(String message) {
        super(message);
        this.venueId = null;
        this.conflictingEventId = null;
    }

    public ScheduleConflictException(String message, Long venueId, Long conflictingEventId) {
        super(message);
        this.venueId = venueId;
        this.conflictingEventId = conflictingEventId;
    }

    public Long getVenueId() {
        return venueId;
    }

    public Long getConflictingEventId() {
        return conflictingEventId;
    }
}
