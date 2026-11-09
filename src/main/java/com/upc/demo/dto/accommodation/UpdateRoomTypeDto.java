package com.upc.demo.dto.accommodation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomTypeDto {

    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String name;

    private String description;

    @Min(value = 1, message = "La capacidad base debe ser al menos 1")
    private Integer baseCapacity;

    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
    private Integer maxCapacity;

    @Min(value = 1, message = "La cantidad de camas debe ser al menos 1")
    private Integer beds;

    @Min(value = 1, message = "La cantidad de unidades debe ser al menos 1")
    private Integer totalUnits;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio base por noche no puede ser negativo")
    private BigDecimal basePricePerNight;

    private List<String> amenities;

    private Boolean isActive;
}
