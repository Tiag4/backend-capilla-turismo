package com.upc.demo.dto.accommodation;

import com.upc.demo.entidad.RatePlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatePlanResponseDto {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal pricePerNight;
    private Boolean includesBreakfast;
    private Boolean freeCancellation;
    private Integer minNights;
    private Boolean isActive;

    public static RatePlanResponseDto fromEntity(RatePlan entity) {
        if (entity == null) {
            return null;
        }
        return RatePlanResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .pricePerNight(entity.getPricePerNight())
                .includesBreakfast(entity.getIncludesBreakfast())
                .freeCancellation(entity.getFreeCancellation())
                .minNights(entity.getMinNights())
                .isActive(entity.getIsActive())
                .build();
    }
}
