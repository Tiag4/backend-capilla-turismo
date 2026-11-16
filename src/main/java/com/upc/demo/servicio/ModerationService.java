package com.upc.demo.servicio;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.moderation.ModerationItemDto;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.enums.ImageModerationStatus;
import com.upc.demo.repositorio.AccommodationImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final AccommodationImageRepository accommodationImageRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ModerationItemDto> getImages(ImageModerationStatus status) {
        List<AccommodationImage> images = status != null
                ? accommodationImageRepository.findByStatusOrderByCreatedAtDesc(status)
                : accommodationImageRepository.findAllByOrderByCreatedAtDesc();

        return images.stream()
                .map(ModerationItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModerationItemDto moderateImage(UUID imageId, ImageModerationStatus targetStatus, String notes, UserPrincipal admin) {
        if (targetStatus != ImageModerationStatus.APPROVED && targetStatus != ImageModerationStatus.REJECTED) {
            throw new BadRequestException("Estado de moderación inválido. Debe ser APPROVED o REJECTED.");
        }

        if (targetStatus == ImageModerationStatus.REJECTED && (notes == null || notes.isBlank())) {
            throw new BadRequestException("Debe indicar el motivo del rechazo u observación para el prestador.");
        }

        AccommodationImage image = accommodationImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Fotografía no encontrada con ID: " + imageId));

        String adminEmail = admin != null ? admin.getUsername() : "admin@capilladelmonte.gov.ar";
        image.setStatus(targetStatus);
        image.setRejectionReason(targetStatus == ImageModerationStatus.REJECTED ? notes.trim() : null);
        image.setModeratedAt(LocalDateTime.now());
        image.setModeratedBy(adminEmail);

        AccommodationImage saved = accommodationImageRepository.save(image);

        String accName = saved.getAccommodation() != null ? saved.getAccommodation().getName() : "Alojamiento";
        String hostEmail = (saved.getAccommodation() != null && saved.getAccommodation().getHost() != null)
                ? saved.getAccommodation().getHost().getEmail()
                : "";

        auditService.logEvent(
                targetStatus == ImageModerationStatus.APPROVED ? "IMAGE_APPROVED" : "IMAGE_REJECTED",
                (targetStatus == ImageModerationStatus.APPROVED ? "Aprobación" : "Rechazo") + " de fotografía en " + accName,
                adminEmail,
                adminEmail,
                hostEmail,
                saved.getId().toString(),
                targetStatus == ImageModerationStatus.REJECTED ? notes.trim() : "Fotografía aprobada para publicación en portal."
        );

        return ModerationItemDto.fromEntity(saved);
    }
}
