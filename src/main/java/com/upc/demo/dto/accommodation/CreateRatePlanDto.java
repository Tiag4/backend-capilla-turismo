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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRatePlanDto {

    @NotBlank(message = "El nombre de la tarifa es obligatorio")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String name;

    private String description;

    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio por noche no puede ser negativo")
    private BigDecimal pricePerNight;

    @Builder.Default
    private Boolean includesBreakfast = false;

    @Builder.Default
    private Boolean freeCancellation = true;

    @Min(value = 1, message = "La estadía mínima debe ser al menos 1 noche")
    @Builder.Default
    private Integer minNights = 1;

    @Builder.Default
    private Boolean isActive = true;
}
