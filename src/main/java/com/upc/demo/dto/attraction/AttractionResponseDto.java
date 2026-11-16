package com.upc.demo.dto.attraction;

import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.enums.AttractionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionResponseDto {

    private UUID id;
    private String name;
    private String description;
    private AttractionCategory category;
    private String difficulty;
    private String estimatedDuration;
    private String howToGet;
    private Boolean requiresGuide;
    private BigDecimal admissionFee;
    private Double latitude;
    private Double longitude;
    private com.upc.demo.entidad.enums.AttractionStatus status;
    private String statusNotice;
    private Boolean isFeatured;

    // Ficha Técnica de Montaña y Telemetría
    private Integer elevationGainMeters;
    private Integer maxElevationMsnm;
    private Double distanceKm;
    private String trailType;
    private String terrainType;

    // Protocolos de Seguridad y Horarios
    private String openingHours;
    private String cutoffTime;
    private String hydrationRequirement;

    @Builder.Default
    private List<String> highlights = new ArrayList<>();

    @Builder.Default
    private List<String> recommendedGear = new ArrayList<>();

    @Builder.Default
    private List<String> safetyWarnings = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<AttractionImageDto> images = new ArrayList<>();

    public static AttractionResponseDto fromEntity(Attraction entity) {
        if (entity == null) {
            return null;
        }

        List<AttractionImageDto> imageDtos = entity.getImages() != null
                ? entity.getImages().stream()
                .map(AttractionImageDto::fromEntity)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return AttractionResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .difficulty(entity.getDifficulty())
                .estimatedDuration(entity.getEstimatedDuration())
                .howToGet(entity.getHowToGet())
                .requiresGuide(entity.getRequiresGuide())
                .admissionFee(entity.getAdmissionFee())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus() != null ? entity.getStatus() : com.upc.demo.entidad.enums.AttractionStatus.OPEN)
                .statusNotice(entity.getStatusNotice())
                .isFeatured(entity.getIsFeatured() != null ? entity.getIsFeatured() : false)
                .elevationGainMeters(entity.getElevationGainMeters())
                .maxElevationMsnm(entity.getMaxElevationMsnm())
                .distanceKm(entity.getDistanceKm())
                .trailType(entity.getTrailType())
                .terrainType(entity.getTerrainType())
                .openingHours(entity.getOpeningHours())
                .cutoffTime(entity.getCutoffTime())
                .hydrationRequirement(entity.getHydrationRequirement())
                .highlights(entity.getHighlights() != null ? new ArrayList<>(entity.getHighlights()) : new ArrayList<>())
                .recommendedGear(entity.getRecommendedGear() != null ? new ArrayList<>(entity.getRecommendedGear()) : new ArrayList<>())
                .safetyWarnings(entity.getSafetyWarnings() != null ? new ArrayList<>(entity.getSafetyWarnings()) : new ArrayList<>())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .images(imageDtos)
                .build();
    }
}
