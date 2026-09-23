package com.upc.demo.dto.accommodation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationSummaryDto {

    private UUID id;
    private String name;
    private String locality;
    private String address;
    private String imageUrl;

    @Builder.Default
    private List<AccommodationImageDto> images = new ArrayList<>();
}