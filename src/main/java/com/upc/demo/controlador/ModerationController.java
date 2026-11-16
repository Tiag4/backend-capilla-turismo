package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.moderation.ModerationActionDto;
import com.upc.demo.dto.moderation.ModerationItemDto;
import com.upc.demo.entidad.enums.ImageModerationStatus;
import com.upc.demo.servicio.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
@Tag(name = "Moderación de Contenidos", description = "Aprobación y rechazo granular de fotografías de alojamientos")
public class ModerationController {

    private final ModerationService moderationService;

    @GetMapping({"/images", "/items"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar fotografías para moderación", description = "Lista fotografías de alojamientos filtrables por estado (PENDING, APPROVED, REJECTED).")
    public ResponseEntity<List<ModerationItemDto>> getImages(
            @RequestParam(required = false) ImageModerationStatus status) {
        return ResponseEntity.ok(moderationService.getImages(status));
    }

    @PatchMapping("/images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Moderar fotografía por ID", description = "Aprueba o rechaza una fotografía individualmente.")
    public ResponseEntity<ModerationItemDto> moderateImage(
            @PathVariable UUID id,
            @RequestBody ModerationActionDto dto,
            @AuthenticationPrincipal UserPrincipal admin) {
        return ResponseEntity.ok(moderationService.moderateImage(id, dto.getStatus(), dto.getNotes(), admin));
    }

    @PatchMapping("/items/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar fotografía", description = "Aprueba una fotografía para que sea visible en el portal turístico.")
    public ResponseEntity<ModerationItemDto> approveImage(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal admin) {
        return ResponseEntity.ok(moderationService.moderateImage(id, ImageModerationStatus.APPROVED, null, admin));
    }

    @PatchMapping("/items/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar fotografía", description = "Rechaza una fotografía indicando el motivo de corrección para el prestador.")
    public ResponseEntity<ModerationItemDto> rejectImage(
            @PathVariable UUID id,
            @RequestBody ModerationActionDto dto,
            @AuthenticationPrincipal UserPrincipal admin) {
        return ResponseEntity.ok(moderationService.moderateImage(id, ImageModerationStatus.REJECTED, dto.getNotes(), admin));
    }
}
