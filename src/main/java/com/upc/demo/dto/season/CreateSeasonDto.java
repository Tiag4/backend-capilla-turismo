package com.upc.demo.dto.season;

import com.upc.demo.entidad.enums.SeasonType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeasonDto {

    @NotBlank(message = "El nombre de la temporada es obligatorio")
    private String name;

    @NotNull(message = "El tipo de temporada es obligatorio")
    private SeasonType type;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;

    @NotNull(message = "El multiplicador tarifario sugerido es obligatorio")
    @DecimalMin(value = "0.1", message = "El multiplicador debe ser mayor a 0")
    private Double suggestedMultiplier;

    private String description;
}
