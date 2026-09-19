package com.upc.demo.entidad;

import com.upc.demo.entidad.enums.AttractionCategory;
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