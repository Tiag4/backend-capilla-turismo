package com.upc.demo.servicio;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.dto.moderation.ModerationItemDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.ImageModerationStatus;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.AccommodationImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock
    private AccommodationImageRepository accommodationImageRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ModerationService moderationService;

    @Test
    @DisplayName("moderateImage debe aprobar fotografía y limpiar rejectionReason")
    void moderateImage_approve_setsApprovedStatus() {
        UUID imgId = UUID.randomUUID();
        AccommodationImage image = buildImage(imgId, ImageModerationStatus.PENDING);
        image.setRejectionReason("Motivo previo");
        UserPrincipal admin = UserPrincipal.create(User.builder()
                .id(UUID.randomUUID())
                .email("admin@capilla.gov.ar")
                .password("")
                .name("Admin")
                .lastName("Turismo")
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        when(accommodationImageRepository.findById(imgId)).thenReturn(Optional.of(image));
        when(accommodationImageRepository.save(any(AccommodationImage.class))).thenAnswer(inv -> inv.getArgument(0));

        ModerationItemDto result = moderationService.moderateImage(imgId, ImageModerationStatus.APPROVED, null, admin);

        assertThat(result.getStatus()).isEqualTo(ImageModerationStatus.APPROVED);
        assertThat(result.getFeedbackNotes()).isNull();
        assertThat(result.getModeratedAt()).isNotNull();
        verify(auditService).logEvent(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("moderateImage debe rechazar fotografía exigiendo motivo")
    void moderateImage_rejectWithoutReason_throwsBadRequest() {
        UUID imgId = UUID.randomUUID();
        UserPrincipal admin = UserPrincipal.create(User.builder()
                .id(UUID.randomUUID())
                .email("admin@capilla.gov.ar")
                .password("")
                .name("Admin")
                .lastName("Turismo")
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        assertThatThrownBy(() -> moderationService.moderateImage(imgId, ImageModerationStatus.REJECTED, "  ", admin))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("getImages debe filtrar por estado cuando se especifica")
    void getImages_withStatus_filtersCorrectly() {
        AccommodationImage pending = buildImage(UUID.randomUUID(), ImageModerationStatus.PENDING);
        when(accommodationImageRepository.findByStatusOrderByCreatedAtDesc(ImageModerationStatus.PENDING))
                .thenReturn(List.of(pending));

        List<ModerationItemDto> result = moderationService.getImages(ImageModerationStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ImageModerationStatus.PENDING);
    }

    private AccommodationImage buildImage(UUID id, ImageModerationStatus status) {
        User host = User.builder().id(UUID.randomUUID()).name("Carla").lastName("Pérez").email("host@capilla.com").build();
        Accommodation acc = Accommodation.builder().id(UUID.randomUUID()).name("Cabañas Sol").host(host).build();
        return AccommodationImage.builder()
                .id(id)
                .url("https://example.com/foto.jpg")
                .isMain(true)
                .status(status)
                .accommodation(acc)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
