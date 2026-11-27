package com.upc.demo.dto.season;

import com.upc.demo.entidad.enums.SeasonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSeasonDto {
    private String name;
    private SeasonType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double suggestedMultiplier;
    private String description;
    private Boolean isActive;
}
