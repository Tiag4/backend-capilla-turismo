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
    private String unitTitle;
    private Integer surfaceSqm;
    private String bedroomsSummary;
    private String phone;
    private String checkInTime;
    private String checkOutTime;
    private String petPolicy;
    private String cancellationPolicy;
    private String licenseNumber;
    private String responseTime;
    private Boolean hasBreakfast;
    private String breakfastType;
    private List<String> amenities;
    private Boolean isActive;
    private Integer downPaymentPercentage;
    private String bankAccountHolder;
    private String bankCbu;
    private String bankAlias;
    private String bankName;
    private UUID hostId;
    private String hostName;
    private UserProfileDto host;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String inventoryMode;

    @Builder.Default
    private List<RoomTypeResponseDto> roomTypes = new ArrayList<>();

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

        List<RoomTypeResponseDto> roomTypeDtos = entity.getRoomTypes() != null
                ? entity.getRoomTypes().stream()
                .filter(rt -> rt.getIsActive() == null || rt.getIsActive())
                .map(RoomTypeResponseDto::fromEntity)
                .collect(Collectors.toList())
                : new ArrayList<>();

        String derivedMode = (!roomTypeDtos.isEmpty()) ? "MULTI_ROOM" : "SINGLE_UNIT";

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
                .unitTitle(entity.getUnitTitle())
                .surfaceSqm(entity.getSurfaceSqm())
                .bedroomsSummary(entity.getBedroomsSummary())
                .phone(entity.getPhone())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .petPolicy(entity.getPetPolicy())
                .cancellationPolicy(entity.getCancellationPolicy())
                .licenseNumber(entity.getLicenseNumber())
                .responseTime(entity.getResponseTime())
                .hasBreakfast(entity.getHasBreakfast() != null ? entity.getHasBreakfast() : false)
                .breakfastType(entity.getBreakfastType())
                .amenities(entity.getAmenities() != null ? new ArrayList<>(entity.getAmenities()) : new ArrayList<>())
                .isActive(entity.getIsActive())
                .downPaymentPercentage(entity.getDownPaymentPercentage() != null ? entity.getDownPaymentPercentage() : 50)
                .bankAccountHolder(entity.getBankAccountHolder())
                .bankCbu(entity.getBankCbu())
                .bankAlias(entity.getBankAlias())
                .bankName(entity.getBankName())
                .hostId(hostUuid)
                .hostName(hostFullName)
                .host(hostDto)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .inventoryMode(derivedMode)
                .roomTypes(roomTypeDtos)
                .images(imageDtos)
                .build();
    }
}