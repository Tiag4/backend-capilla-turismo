package com.upc.demo.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pharmacies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(length = 50)
    private String whatsapp;

    @Column(name = "duty_hours", nullable = false, length = 100)
    private String dutyHours;

    @Column(name = "valid_from", length = 30)
    private String validFrom;

    @Column(name = "valid_to", length = 30)
    private String validTo;

    @Column(name = "is_on_duty_today", nullable = false)
    @Builder.Default
    private boolean isOnDutyToday = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "google_maps_url", length = 500)
    private String googleMapsUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
