package com.upc.demo.dto.attraction;

import com.upc.demo.entidad.AttractionImage;
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
public class AttractionImageDto {

    private UUID id;

    @NotBlank(message = "Image URL is required")
    private String url;

    private String publicId;

    private LocalDateTime createdAt;

    public static AttractionImageDto fromEntity(AttractionImage entity) {
        if (entity == null) {
            return null;
        }
        return AttractionImageDto.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .publicId(entity.getPublicId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}