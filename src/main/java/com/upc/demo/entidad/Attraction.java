package com.upc.demo.entidad;

import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.entidad.enums.AttractionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AttractionCategory category = AttractionCategory.HILL;

    @Column(length = 50)
    private String difficulty;

    @Column(name = "estimated_duration", length = 100)
    private String estimatedDuration;

    @Column(name = "how_to_get", columnDefinition = "TEXT")
    private String howToGet;

    @Column(name = "requires_guide", nullable = false)
    @Builder.Default
    private Boolean requiresGuide = false;

    @Column(name = "admission_fee", precision = 10, scale = 2)
    private BigDecimal admissionFee;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private AttractionStatus status = AttractionStatus.OPEN;

    @Column(name = "status_notice")
    private String statusNotice;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "featured_badge_theme", length = 20)
    @Builder.Default
    private String featuredBadgeTheme = "DARK";

    // Ficha Técnica de Montaña y Telemetría
    @Column(name = "elevation_gain_meters")
    private Integer elevationGainMeters;

    @Column(name = "max_elevation_msnm")
    private Integer maxElevationMsnm;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "trail_type", length = 100)
    private String trailType;

    @Column(name = "terrain_type", length = 200)
    private String terrainType;

    // Protocolos de Seguridad y Horarios
    @Column(name = "opening_hours", length = 150)
    private String openingHours;

    @Column(name = "cutoff_time", length = 150)
    private String cutoffTime;

    @Column(name = "hydration_requirement", length = 200)
    private String hydrationRequirement;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attraction_highlights", joinColumns = @JoinColumn(name = "attraction_id"))
    @Column(name = "highlight", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> highlights = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attraction_gear", joinColumns = @JoinColumn(name = "attraction_id"))
    @Column(name = "gear", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> recommendedGear = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attraction_safety_warnings", joinColumns = @JoinColumn(name = "attraction_id"))
    @Column(name = "warning", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> safetyWarnings = new ArrayList<>();

    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AttractionImage> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
