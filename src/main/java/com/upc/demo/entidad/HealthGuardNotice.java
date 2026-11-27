package com.upc.demo.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "health_guard_notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthGuardNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "facility_name", nullable = false, length = 180)
    private String facilityName;

    @Column(name = "emergency_phone", nullable = false, length = 50)
    private String emergencyPhone;

    @Column(name = "direct_phone", nullable = false, length = 50)
    private String directPhone;

    @Column(nullable = false, length = 200)
    private String address;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "health_guard_active_guards", joinColumns = @JoinColumn(name = "health_guard_id"))
    @Column(name = "guard_description", nullable = false)
    @Builder.Default
    private List<String> activeGuards = new ArrayList<>();

    @Column(name = "special_notice", columnDefinition = "TEXT")
    private String specialNotice;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
