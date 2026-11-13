package com.upc.demo.dto.sanitary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthGuardNoticeDto {
    private UUID id;
    private String facilityName;
    private String emergencyPhone;
    private String directPhone;
    private String address;
    private List<String> activeGuards;
    private String specialNotice;
    private LocalDateTime updatedAt;
}
