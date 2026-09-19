package com.upc.demo.dto.attraction;

import com.upc.demo.entidad.enums.AttractionCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
public class CreateAttractionDto {

    @NotBlank(message = "El nombre del atractivo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String name;

    private String description;

    @NotNull(message = "La categoria es obligatoria")
    private AttractionCategory category;

    @Size(max = 50, message = "La dificultad no puede exceder 50 caracteres")
    private String difficulty;

    @Size(max = 100, message = "La duracion estimada no puede exceder 100 caracteres")
    private String estimatedDuration;

    private String howToGet;

    @Builder.Default
    private Boolean requiresGuide = false;

    @DecimalMin(value = "0.0", inclusive = true, message = "El costo de entrada no puede ser negativo")
    private BigDecimal admissionFee;

    private Double latitude;

    private Double longitude;

    @Valid
    @Builder.Default
    private List<AttractionImageDto> images = new ArrayList<>();
}