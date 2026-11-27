package com.upc.demo.dto.accommodation;

import com.upc.demo.entidad.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeAvailabilityDto {

    private UUID id;
    private String name;
    private String description;
    private Integer baseCapacity;
    private Integer maxCapacity;
    private Integer beds;
    private Integer totalUnits;
    private Integer availableUnits;
    private BigDecimal basePricePerNight;
    private List<String> amenities;
    private List<RatePlanResponseDto> ratePlans;

    public static RoomTypeAvailabilityDto fromEntity(RoomType entity, int availableUnits) {
        if (entity == null) {
            return null;
        }

        List<RatePlanResponseDto> planDtos = entity.getRatePlans() != null
                ? entity.getRatePlans().stream()
                .map(RatePlanResponseDto::fromEntity)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return RoomTypeAvailabilityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .baseCapacity(entity.getBaseCapacity())
                .maxCapacity(entity.getMaxCapacity())
                .beds(entity.getBeds())
                .totalUnits(entity.getTotalUnits())
                .availableUnits(availableUnits)
                .basePricePerNight(entity.getBasePricePerNight())
                .amenities(entity.getAmenities() != null ? new ArrayList<>(entity.getAmenities()) : new ArrayList<>())
                .ratePlans(planDtos)
                .build();
    }
}
