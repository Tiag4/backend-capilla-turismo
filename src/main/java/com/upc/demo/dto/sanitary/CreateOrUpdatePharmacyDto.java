package com.upc.demo.dto.sanitary;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdatePharmacyDto {
    private String id;

    @NotBlank(message = "El nombre de la farmacia es obligatorio")
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String phone;

    private String whatsapp;

    @NotBlank(message = "El horario o modalidad de guardia es obligatorio")
    private String dutyHours;

    private String validFrom;
    private String validTo;
    private Boolean isOnDutyToday;
    private String notes;
    private String googleMapsUrl;
}
