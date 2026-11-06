package com.upc.demo.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewDto {

    @NotBlank(message = "El código de reserva es obligatorio")
    private String bookingCode;

    @NotNull(message = "La calificación general es obligatoria")
    @Min(value = 1, message = "La calificación general debe ser entre 1 y 5")
    @Max(value = 5, message = "La calificación general debe ser entre 1 y 5")
    private Integer rating;

    @NotNull(message = "La calificación de limpieza es obligatoria")
    @Min(value = 1, message = "La calificación de limpieza debe ser entre 1 y 5")
    @Max(value = 5, message = "La calificación de limpieza debe ser entre 1 y 5")
    private Integer cleanlinessRating;

    @NotNull(message = "La calificación del anfitrión es obligatoria")
    @Min(value = 1, message = "La calificación del anfitrión debe ser entre 1 y 5")
    @Max(value = 5, message = "La calificación del anfitrión debe ser entre 1 y 5")
    private Integer hostRating;

    @NotNull(message = "La calificación de ubicación es obligatoria")
    @Min(value = 1, message = "La calificación de ubicación debe ser entre 1 y 5")
    @Max(value = 5, message = "La calificación de ubicación debe ser entre 1 y 5")
    private Integer locationRating;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 20, max = 2000, message = "El comentario debe tener entre 20 y 2000 caracteres")
    private String comment;

    @Size(max = 100, message = "El origen del huésped no puede superar los 100 caracteres")
    private String guestOrigin;

    @Size(max = 60, message = "La fecha de estadía no puede superar los 60 caracteres")
    private String stayDate;
}
