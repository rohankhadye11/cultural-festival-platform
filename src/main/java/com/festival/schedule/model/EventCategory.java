package com.festival.schedule.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Event category master data entity (e.g., Music, Dance, Drama, Tech, Fine Arts).
 */
@Entity
@Table(name = "event_categories")
public class EventCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @NotBlank
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color code must be a valid 6-character hex string (e.g. #3B82F6)")
    @Column(name = "color_code", nullable = false, length = 7)
    private String colorCode = "#3B82F6";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public EventCategory() {
    }

    public EventCategory(String name, String description, String colorCode) {
        this.name = name;
        this.description = description;
        this.colorCode = colorCode != null ? colorCode : "#3B82F6";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
