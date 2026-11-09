package com.upc.demo.dto.accommodation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomTypeDto {

    @NotBlank(message = "El nombre del tipo de habitación es obligatorio")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String name;

    private String description;

    @Min(value = 1, message = "La capacidad base debe ser al menos 1")
    @Builder.Default
    private Integer baseCapacity = 1;

    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
    @Builder.Default
    private Integer maxCapacity = 2;

    @Min(value = 1, message = "La cantidad de camas debe ser al menos 1")
    @Builder.Default
    private Integer beds = 1;

    @NotNull(message = "La cantidad de unidades es obligatoria")
    @Min(value = 1, message = "La cantidad de unidades debe ser al menos 1")
    private Integer totalUnits;

    @NotNull(message = "El precio base por noche es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio base por noche no puede ser negativo")
    private BigDecimal basePricePerNight;

    @Builder.Default
    private List<String> amenities = new ArrayList<>();
}
