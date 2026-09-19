package com.upc.demo.dto.accommodation;

import com.upc.demo.dto.auth.UserProfileDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.enums.AccommodationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationResponseDto {

    private UUID id;
    private String name;
    private String description;
    private AccommodationType type;
    private String address;
    private String locality;
    private Double latitude;
    private Double longitude;
    private BigDecimal pricePerNight;
    private Integer maxGuests;
    private List<String> amenities;
    private Boolean isActive;
    private UUID hostId;
    private String hostName;
    private UserProfileDto host;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<AccommodationImageDto> images = new ArrayList<>();

    public static AccommodationResponseDto fromEntity(Accommodation entity) {
        if (entity == null) {
            return null;
        }

        List<AccommodationImageDto> imageDtos = entity.getImages() != null
                ? entity.getImages().stream()
                .map(AccommodationImageDto::fromEntity)
                .collect(Collectors.toList())
                : new ArrayList<>();

        UserProfileDto hostDto = entity.getHost() != null
                ? UserProfileDto.fromEntity(entity.getHost())
                : null;

        UUID hostUuid = entity.getHost() != null ? entity.getHost().getId() : null;
        String hostFullName = entity.getHost() != null
                ? (entity.getHost().getName() + " " + entity.getHost().getLastName()).trim()
                : null;

        return AccommodationResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .address(entity.getAddress())
                .locality(entity.getLocality())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .pricePerNight(entity.getPricePerNight())
                .maxGuests(entity.getMaxGuests())
                .amenities(entity.getAmenities() != null ? new ArrayList<>(entity.getAmenities()) : new ArrayList<>())
                .isActive(entity.getIsActive())
                .hostId(hostUuid)
                .hostName(hostFullName)
                .host(hostDto)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .images(imageDtos)
                .build();
    }
}