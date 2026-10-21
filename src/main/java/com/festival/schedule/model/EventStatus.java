package com.festival.schedule.model;

/**
 * Event lifecycle state machine values.
 */
public enum EventStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    CANCELLED
}
