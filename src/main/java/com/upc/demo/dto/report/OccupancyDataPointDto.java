package com.upc.demo.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyDataPointDto {
    private String label;
    private Double rate;
    private Integer bookingsCount;
}
