package com.upc.demo.dto.sanitary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetPharmacyOnDutyDto {
    private String validFrom;
    private String validTo;
}
