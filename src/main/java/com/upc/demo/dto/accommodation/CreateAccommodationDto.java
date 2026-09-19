package com.upc.demo.dto.accommodation;

import com.upc.demo.entidad.enums.AccommodationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class CreateAccommodationDto {

    @NotBlank(message = "El nombre del alojamiento es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String name;

    private String description;

    @NotNull(message = "El tipo de alojamiento es obligatorio")
    private AccommodationType type;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 200, message = "La direccion no puede exceder 200 caracteres")
    private String address;

    @Builder.Default
    private String locality = "Capilla del Monte";

    private Double latitude;

    private Double longitude;

    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio por noche no puede ser negativo")
    private BigDecimal pricePerNight;

    @NotNull(message = "La capacidad maxima de huespedes es obligatoria")
    @Min(value = 1, message = "La capacidad maxima debe ser al menos 1 huesped")
    private Integer maxGuests;

    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<AccommodationImageDto> images = new ArrayList<>();
}