package com.upc.demo.dto.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateBookingDto {

    @NotNull(message = "El ID del alojamiento es obligatorio")
    private UUID accommodationId;

    @NotNull(message = "La fecha de check-in es obligatoria")
    @FutureOrPresent(message = "La fecha de check-in debe ser hoy o una fecha futura")
    private LocalDate checkIn;

    @NotNull(message = "La fecha de check-out es obligatoria")
    @Future(message = "La fecha de check-out debe ser una fecha futura")
    private LocalDate checkOut;

    @NotNull(message = "La cantidad de huéspedes es obligatoria")
    @Min(value = 1, message = "La cantidad de huéspedes debe ser al menos 1")
    private Integer guestCount;

    @NotBlank(message = "El nombre del huésped es obligatorio")
    private String guestName;

    @NotBlank(message = "El email del huésped es obligatorio")
    @Email(message = "El formato de email no es válido")
    private String guestEmail;

    @NotBlank(message = "El teléfono del huésped es obligatorio")
    private String guestPhone;

    private String guestOrigin;

    private String notes;
}
