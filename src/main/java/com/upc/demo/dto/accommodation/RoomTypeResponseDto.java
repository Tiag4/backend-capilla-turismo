package com.upc.demo.dto.accommodation;

import com.upc.demo.entidad.RoomType;
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
public class RoomTypeResponseDto {

    private UUID id;
    private UUID accommodationId;
    private String name;
    private String description;
    private Integer baseCapacity;
    private Integer maxCapacity;
    private Integer beds;
    private Integer totalUnits;
    private BigDecimal basePricePerNight;
    private Boolean isActive;
    private List<String> amenities;
    private List<RatePlanResponseDto> ratePlans;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RoomTypeResponseDto fromEntity(RoomType entity) {
        if (entity == null) {
            return null;
        }

        List<RatePlanResponseDto> planDtos = entity.getRatePlans() != null
                ? entity.getRatePlans().stream()
                .map(RatePlanResponseDto::fromEntity)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return RoomTypeResponseDto.builder()
                .id(entity.getId())
                .accommodationId(entity.getAccommodation() != null ? entity.getAccommodation().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .baseCapacity(entity.getBaseCapacity())
                .maxCapacity(entity.getMaxCapacity())
                .beds(entity.getBeds())
                .totalUnits(entity.getTotalUnits())
                .basePricePerNight(entity.getBasePricePerNight())
                .isActive(entity.getIsActive())
                .amenities(entity.getAmenities() != null ? new ArrayList<>(entity.getAmenities()) : new ArrayList<>())
                .ratePlans(planDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
