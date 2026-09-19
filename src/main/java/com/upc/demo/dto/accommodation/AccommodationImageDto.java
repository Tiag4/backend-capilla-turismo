package com.upc.demo.dto.accommodation;

import com.upc.demo.entidad.AccommodationImage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationImageDto {

    private UUID id;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String url;

    private String publicId;

    private Boolean isMain;

    private LocalDateTime createdAt;

    public static AccommodationImageDto fromEntity(AccommodationImage entity) {
        if (entity == null) {
            return null;
        }
        return AccommodationImageDto.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .publicId(entity.getPublicId())
                .isMain(entity.getIsMain())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
