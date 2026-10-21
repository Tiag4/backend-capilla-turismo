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