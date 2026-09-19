package com.upc.demo.dto.attraction;

import com.upc.demo.entidad.enums.AttractionCategory;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateAttractionDto {

    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String name;

    private String description;

    private AttractionCategory category;

    @Size(max = 50, message = "La dificultad no puede exceder 50 caracteres")
    private String difficulty;

    @Size(max = 100, message = "La duracion estimada no puede exceder 100 caracteres")
    private String estimatedDuration;

    private String howToGet;

    private Boolean requiresGuide;

    @DecimalMin(value = "0.0", inclusive = true, message = "El costo de entrada no puede ser negativo")
    private BigDecimal admissionFee;

    private Double latitude;

    private Double longitude;
}