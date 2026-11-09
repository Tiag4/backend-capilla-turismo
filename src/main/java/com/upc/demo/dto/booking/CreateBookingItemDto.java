package com.upc.demo.dto.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingItemDto {

    @NotNull(message = "El ID del tipo de habitación es obligatorio")
    private UUID roomTypeId;

    private UUID ratePlanId;

    @NotNull(message = "La cantidad de unidades es obligatoria")
    @Min(value = 1, message = "La cantidad de unidades debe ser al menos 1")
    private Integer quantity;

    @NotNull(message = "La cantidad de huéspedes es obligatoria")
    @Min(value = 1, message = "La cantidad de huéspedes debe ser al menos 1")
    private Integer guests;
}
