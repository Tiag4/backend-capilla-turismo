package com.upc.demo.dto.accommodation;

import com.upc.demo.entidad.enums.AccommodationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class UpdateAccommodationDto {

    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String name;

    private String description;

    private AccommodationType type;

    @Size(max = 200, message = "La direccion no puede exceder 200 caracteres")
    private String address;

    @Size(max = 100, message = "La localidad no puede exceder 100 caracteres")
    private String locality;

    private Double latitude;

    private Double longitude;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio por noche no puede ser negativo")
    private BigDecimal pricePerNight;

    @Min(value = 1, message = "La capacidad maxima debe ser al menos 1 huesped")
    private Integer maxGuests;

    @Size(max = 150, message = "El título de la unidad no puede exceder 150 caracteres")
    private String unitTitle;

    @Min(value = 1, message = "La superficie debe ser al menos 1 m²")
    private Integer surfaceSqm;

    @Size(max = 300, message = "El resumen de dormitorios no puede exceder 300 caracteres")
    private String bedroomsSummary;

    @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
    private String phone;

    @Size(max = 20, message = "El horario de check-in no puede exceder 20 caracteres")
    private String checkInTime;

    @Size(max = 20, message = "El horario de check-out no puede exceder 20 caracteres")
    private String checkOutTime;

    @Size(max = 250, message = "La política de mascotas no puede exceder 250 caracteres")
    private String petPolicy;

    @Size(max = 300, message = "La política de cancelación no puede exceder 300 caracteres")
    private String cancellationPolicy;

    @Size(max = 60, message = "El número de habilitación o matrícula no puede exceder 60 caracteres")
    private String licenseNumber;

    @Size(max = 60, message = "El tiempo de respuesta no puede exceder 60 caracteres")
    private String responseTime;

    private Boolean hasBreakfast;

    @Size(max = 150, message = "El tipo de desayuno no puede exceder 150 caracteres")
    private String breakfastType;

    private List<String> amenities;

    @Min(value = 0, message = "El porcentaje de seña no puede ser negativo")
    @Max(value = 100, message = "El porcentaje de seña no puede superar el 100%")
    private Integer downPaymentPercentage;

    @Size(max = 150, message = "El titular de la cuenta no puede exceder 150 caracteres")
    private String bankAccountHolder;

    @Size(max = 30, message = "El CBU/CVU no puede exceder 30 caracteres")
    private String bankCbu;

    @Size(max = 60, message = "El alias bancario no puede exceder 60 caracteres")
    private String bankAlias;

    @Size(max = 100, message = "El nombre de la entidad bancaria no puede exceder 100 caracteres")
    private String bankName;

    private Boolean isActive;
}