package com.upc.demo.dto.moderation;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.ImageModerationStatus;
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
public class ModerationItemDto {

    private UUID id;
    private UUID accommodationId;
    private String accommodationName;
    private String hostName;
    private String hostEmail;
    private String imageUrl;
    private Boolean isMain;
    private String caption;
    private ImageModerationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime moderatedAt;
    private String feedbackNotes;

    public static ModerationItemDto fromEntity(AccommodationImage image) {
        Accommodation acc = image.getAccommodation();
        User host = acc != null ? acc.getHost() : null;
        String hostFullName = host != null
                ? ((host.getName() != null ? host.getName() : "") + " " + (host.getLastName() != null ? host.getLastName() : "")).trim()
                : "Prestador";

        return ModerationItemDto.builder()
                .id(image.getId())
                .accommodationId(acc != null ? acc.getId() : null)
                .accommodationName(acc != null ? acc.getName() : "Alojamiento")
                .hostName(hostFullName)
                .hostEmail(host != null ? host.getEmail() : "")
                .imageUrl(image.getUrl())
                .isMain(image.getIsMain())
                .caption(Boolean.TRUE.equals(image.getIsMain()) ? "Fotografía de portada" : "Fotografía de galería")
                .status(image.getStatus() != null ? image.getStatus() : ImageModerationStatus.PENDING)
                .submittedAt(image.getCreatedAt())
                .moderatedAt(image.getModeratedAt())
                .feedbackNotes(image.getRejectionReason())
                .build();
    }
}
