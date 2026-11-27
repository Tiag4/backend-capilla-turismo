package com.upc.demo.entidad;

import com.upc.demo.util.UuidV7;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "room_types",
    indexes = @Index(name = "idx_room_type_accommodation", columnList = "accommodation_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_capacity", nullable = false)
    @Builder.Default
    private Integer baseCapacity = 1;

    @Column(name = "max_capacity", nullable = false)
    @Builder.Default
    private Integer maxCapacity = 2;

    @Column(nullable = false)
    @Builder.Default
    private Integer beds = 1;

    @Column(name = "total_units", nullable = false)
    @Builder.Default
    private Integer totalUnits = 1;

    @Column(name = "base_price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerNight;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "room_type_amenities", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "amenity")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RatePlan> ratePlans = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UuidV7.generate();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
