package com.festival.schedule.repository;

import com.festival.schedule.model.Event;
import com.festival.schedule.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusOrderByStartTimeAsc(EventStatus status);

    List<Event> findByVenueIdOrderByStartTimeAsc(Long venueId);

    List<Event> findByCategoryIdOrderByStartTimeAsc(Long categoryId);

    List<Event> findByOrganizerIdOrderByStartTimeAsc(Long organizerId);

    long countByStatus(EventStatus status);

    long countByVenueId(Long venueId);

    /**
     * Finds any existing APPROVED event at the given venue that conflicts with the proposed time window.
     * Overlap condition:
     *   max(event.startTime, :proposedStart) < min(event.endTime, :proposedEnd)
     * which translates to:
     *   event.startTime < :proposedEnd AND event.endTime > :proposedStart
     *
     * @param venueId Target venue ID
     * @param startTime Proposed start timestamp
     * @param endTime Proposed end timestamp
     * @param excludeEventId Optional event ID to exclude (used during updates)
     * @return List of conflicting events
     */
    @Query("""
        SELECT e FROM Event e 
        WHERE e.venue.id = :venueId 
          AND e.status = 'APPROVED'
          AND (:excludeEventId IS NULL OR e.id != :excludeEventId)
          AND (e.startTime < :endTime AND e.endTime > :startTime)
    """)
    List<Event> findConflictingApprovedEvents(
            @Param("venueId") Long venueId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeEventId") Long excludeEventId
    );

    /**
     * Filter approved events by optional category, venue, and time window.
     */
    @Query("""
        SELECT e FROM Event e 
        WHERE (:status IS NULL OR e.status = :status)
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:venueId IS NULL OR e.venue.id = :venueId)
          AND (cast(:fromTime as timestamp) IS NULL OR e.startTime >= :fromTime)
          AND (cast(:toTime as timestamp) IS NULL OR e.endTime <= :toTime)
        ORDER BY e.startTime ASC
    """)
    List<Event> filterEvents(
            @Param("status") EventStatus status,
            @Param("categoryId") Long categoryId,
            @Param("venueId") Long venueId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
    );
}
