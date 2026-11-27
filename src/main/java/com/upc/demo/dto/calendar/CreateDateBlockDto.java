package com.upc.demo.dto.calendar;

import com.upc.demo.entidad.enums.DateBlockReason;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDateBlockDto {

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o una fecha futura")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser una fecha futura")
    private LocalDate endDate;

    private UUID roomTypeId;

    @Min(value = 1, message = "La cantidad de unidades a bloquear debe ser al menos 1")
    @Builder.Default
    private Integer unitsBlocked = 1;

    private DateBlockReason reason;

    @Size(max = 500, message = "La nota no puede superar los 500 caracteres")
    private String notes;
}
