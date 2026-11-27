package com.upc.demo.dto.event;

import com.upc.demo.entidad.enums.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateEventDto {

    @NotBlank(message = "El título del evento es obligatorio")
    @Size(max = 180, message = "El título no puede superar los 180 caracteres")
    private String title;

    @NotBlank(message = "La descripción del evento es obligatoria")
    private String description;

    @NotNull(message = "La categoría del evento es obligatoria")
    private EventCategory category;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 60, message = "El horario no puede superar los 60 caracteres")
    private String timeString;

    @NotBlank(message = "La ubicación del evento es obligatoria")
    @Size(max = 200, message = "La ubicación no puede superar los 200 caracteres")
    private String location;

    @Builder.Default
    private boolean isFree = true;

    private BigDecimal price;

    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
    private String imageUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isFeatured = false;
}
