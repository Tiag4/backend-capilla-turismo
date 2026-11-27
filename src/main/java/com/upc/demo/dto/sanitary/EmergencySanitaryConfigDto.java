package com.upc.demo.dto.sanitary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencySanitaryConfigDto {
    private List<PharmacyResponseDto> pharmacies;
    private HealthGuardNoticeDto healthGuard;
    private LocalDateTime lastUpdated;
}
