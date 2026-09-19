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
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .images(imageDtos)
                .build();
    }
}