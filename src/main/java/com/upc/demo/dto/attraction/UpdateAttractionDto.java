package com.upc.demo.dto.attraction;

import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.entidad.enums.AttractionStatus;
import jakarta.validation.constraints.DecimalMin;
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

    private AttractionStatus status;

    private String statusNotice;

    private Boolean isFeatured;

    private String featuredBadgeTheme;

    // Ficha Técnica de Montaña y Telemetría
    private Integer elevationGainMeters;
    private Integer maxElevationMsnm;
    private Double distanceKm;
    private String trailType;
    private String terrainType;

    // Protocolos de Seguridad y Horarios
    private String openingHours;
    private String cutoffTime;
    private String hydrationRequirement;

    private List<String> highlights;
    private List<String> recommendedGear;
    private List<String> safetyWarnings;
}
