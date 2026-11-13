package com.upc.demo.dto.sanitary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHealthGuardNoticeDto {
    private String facilityName;
    private String emergencyPhone;
    private String directPhone;
    private String address;
    private List<String> activeGuards;
    private String specialNotice;
}
