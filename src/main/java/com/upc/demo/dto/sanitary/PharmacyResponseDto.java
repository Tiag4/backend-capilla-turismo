package com.upc.demo.dto.sanitary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyResponseDto {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String whatsapp;
    private String dutyHours;
    private String validFrom;
    private String validTo;
    private boolean isOnDutyToday;
    private String notes;
    private String googleMapsUrl;
}
