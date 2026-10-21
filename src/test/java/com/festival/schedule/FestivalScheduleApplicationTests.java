package com.festival.schedule;

import com.festival.schedule.exception.ResourceNotFoundException;
import com.festival.schedule.exception.ScheduleConflictException;
import com.festival.schedule.model.*;
import com.festival.schedule.repository.EventRepository;
import com.festival.schedule.repository.VenueRepository;
import com.festival.schedule.service.ConflictDetectionEngine;
import com.festival.schedule.service.EventService;
import com.festival.schedule.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test suite verifying core conflict detection algorithms, Venue CRUD, and Event workflows.
 */
@ExtendWith(MockitoExtension.class)
class FestivalScheduleApplicationTests {

    @Nested
    @DisplayName("Conflict Detection Engine Tests")
    class ConflictEngineTests {

        @Mock
        private EventRepository eventRepository;

        @InjectMocks
        private ConflictDetectionEngine conflictEngine;

        private Venue mainAuditorium;
        private EventCategory musicCategory;
        private User organizer;

        @BeforeEach
        void setUp() {
            mainAuditorium = new Venue("Main Auditorium", "Academic Complex B", 1200, "Full Acoustics");
            mainAuditorium.setId(1L);

            musicCategory = new EventCategory("Western Acoustic", "Solo and band performances", "#8B5CF6");
            musicCategory.setId(1L);

            organizer = new User("sarah_music", "sarah@fest.edu", "hashed_pwd", UserRole.ORGANIZER, "Sarah Connor", "Music Club");
            organizer.setId(10L);
        }

        @Test
        @DisplayName("Should detect conflict when proposed time window overlaps existing approved event")
        void shouldDetectConflictOnOverlap() {
            LocalDateTime baseTime = LocalDateTime.of(2026, 11, 15, 14, 0);
            LocalDateTime existingStart = baseTime;
            LocalDateTime existingEnd = baseTime.plusHours(2); // 14:00 - 16:00

            Event existingEvent = new Event("Battle of the Bands", "Flagship rock competition",
                    musicCategory, mainAuditorium, organizer, existingStart, existingEnd, 500);
            existingEvent.setId(100L);
            existingEvent.setStatus(EventStatus.APPROVED);

            LocalDateTime proposedStart = baseTime.plusHours(1); // 15:00
            LocalDateTime proposedEnd = baseTime.plusHours(3);   // 17:00

            when(eventRepository.findConflictingApprovedEvents(eq(1L), eq(proposedStart), eq(proposedEnd), isNull()))
                    .thenReturn(List.of(existingEvent));

            List<Event> conflicts = conflictEngine.findConflicts(1L, proposedStart, proposedEnd, null);

            assertFalse(conflicts.isEmpty(), "Engine must detect the overlapping event");
            assertEquals(1, conflicts.size());
            assertEquals("Battle of the Bands", conflicts.get(0).getTitle());
            assertTrue(conflictEngine.hasConflict(1L, proposedStart, proposedEnd, null));

            assertThrows(ScheduleConflictException.class, () ->
                    conflictEngine.validateNoConflict(1L, proposedStart, proposedEnd, null)
            );
        }

        @Test
        @DisplayName("Should permit scheduling when venue is completely free")
        void shouldPermitWhenNoConflictExists() {
            LocalDateTime proposedStart = LocalDateTime.of(2026, 11, 15, 18, 0);
            LocalDateTime proposedEnd = LocalDateTime.of(2026, 11, 15, 20, 0);

            when(eventRepository.findConflictingApprovedEvents(eq(1L), eq(proposedStart), eq(proposedEnd), isNull()))
                    .thenReturn(Collections.emptyList());

            List<Event> conflicts = conflictEngine.findConflicts(1L, proposedStart, proposedEnd, null);

            assertTrue(conflicts.isEmpty(), "Engine must return empty conflict list when slot is free");
            assertFalse(conflictEngine.hasConflict(1L, proposedStart, proposedEnd, null));
            assertDoesNotThrow(() -> conflictEngine.validateNoConflict(1L, proposedStart, proposedEnd, null));
        }

        @Test
        @DisplayName("Should reject invalid time intervals where end time is before start time")
        void shouldRejectInvalidIntervals() {
            LocalDateTime start = LocalDateTime.of(2026, 11, 15, 16, 0);
            LocalDateTime end = LocalDateTime.of(2026, 11, 15, 14, 0); // End before start!

            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                    conflictEngine.findConflicts(1L, start, end, null)
            );

            assertEquals("End time must be strictly after start time", thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("Venue Service CRUD Tests")
    class VenueServiceTests {

        @Mock
        private VenueRepository venueRepository;

        @InjectMocks
        private VenueService venueService;

        @Test
        @DisplayName("Should create venue successfully when name is unique and capacity is valid")
        void shouldCreateVenueSuccessfully() {
            Venue newVenue = new Venue("Open Air Amphitheatre", "Zone C", 2500, "Stage Lighting");
            when(venueRepository.existsByNameIgnoreCase("Open Air Amphitheatre")).thenReturn(false);
            when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> {
                Venue v = invocation.getArgument(0);
                v.setId(5L);
                return v;
            });

            Venue created = venueService.createVenue(newVenue);

            assertNotNull(created.getId());
            assertEquals("Open Air Amphitheatre", created.getName());
            assertEquals(2500, created.getCapacity());
            verify(venueRepository, times(1)).save(any(Venue.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when venue name already exists")
        void shouldThrowExceptionOnDuplicateVenueName() {
            Venue duplicate = new Venue("Main Auditorium", "Complex A", 1000, null);
            when(venueRepository.existsByNameIgnoreCase("Main Auditorium")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    venueService.createVenue(duplicate)
            );

            assertTrue(ex.getMessage().contains("already exists"));
            verify(venueRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when venue ID does not exist")
        void shouldThrowNotFoundForNonExistentVenue() {
            when(venueRepository.findById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                    venueService.getVenueById(999L)
            );

            assertTrue(ex.getMessage().contains("Venue not found with ID: 999"));
        }

        @Test
        @DisplayName("Should update venue properties correctly")
        void shouldUpdateVenueSuccessfully() {
            Venue existing = new Venue("Seminar Hall 1", "Block D", 200, "Projector");
            existing.setId(3L);

            when(venueRepository.findById(3L)).thenReturn(Optional.of(existing));
            when(venueRepository.existsByNameIgnoreCaseAndIdNot("Seminar Hall 1 (Upgraded)", 3L)).thenReturn(false);
            when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Venue updates = new Venue("Seminar Hall 1 (Upgraded)", "Block D", 350, "Projector + Sound");
            Venue updated = venueService.updateVenue(3L, updates);

            assertEquals("Seminar Hall 1 (Upgraded)", updated.getName());
            assertEquals(350, updated.getCapacity());
        }

        @Test
        @DisplayName("Should delete existing venue")
        void shouldDeleteExistingVenue() {
            Venue venue = new Venue("Old Hall", "Block A", 100, null);
            venue.setId(7L);

            when(venueRepository.findById(7L)).thenReturn(Optional.of(venue));
            doNothing().when(venueRepository).delete(venue);

            assertDoesNotThrow(() -> venueService.deleteVenue(7L));
            verify(venueRepository, times(1)).delete(venue);
        }
    }

    @Nested
    @DisplayName("Event Service & Summary Dashboard Tests")
    class EventServiceTests {

        @Mock
        private EventRepository eventRepository;

        @Mock
        private VenueRepository venueRepository;

        @Mock
        private ConflictDetectionEngine conflictEngine;

        @InjectMocks
        private EventService eventService;

        @Test
        @DisplayName("Should compute dashboard summary statistics correctly")
        void shouldComputeDashboardSummary() {
            when(eventRepository.count()).thenReturn(15L);
            when(eventRepository.countByStatus(EventStatus.APPROVED)).thenReturn(8L);
            when(eventRepository.countByStatus(EventStatus.PENDING_APPROVAL)).thenReturn(4L);
            when(eventRepository.countByStatus(EventStatus.DRAFT)).thenReturn(1L);
            when(eventRepository.countByStatus(EventStatus.REJECTED)).thenReturn(1L);
            when(eventRepository.countByStatus(EventStatus.CANCELLED)).thenReturn(1L);
            when(venueRepository.count()).thenReturn(6L);
            when(venueRepository.findByIsActiveTrue()).thenReturn(List.of(new Venue(), new Venue(), new Venue()));

            Map<String, Object> summary = eventService.getEventSummary();

            assertEquals(15L, summary.get("totalEvents"));
            assertEquals(8L, summary.get("totalApproved"));
            assertEquals(4L, summary.get("totalPendingApproval"));
            assertEquals(6L, summary.get("totalVenuesConfigured"));
            assertEquals(3, summary.get("activeVenuesCount"));
        }
    }
}
