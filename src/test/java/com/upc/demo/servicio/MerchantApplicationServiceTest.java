package com.upc.demo.servicio;

import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.merchant.CreateMerchantApplicationDto;
import com.upc.demo.dto.merchant.MerchantApplicationResponseDto;
import com.upc.demo.dto.merchant.UpdateMerchantApplicationStatusDto;
import com.upc.demo.entidad.MerchantApplication;
import com.upc.demo.entidad.enums.MerchantApplicationStatus;
import com.upc.demo.repositorio.MerchantApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantApplicationServiceTest {

    @Mock
    private MerchantApplicationRepository merchantApplicationRepository;

    @InjectMocks
    private MerchantApplicationService merchantApplicationService;

    @Test
    @DisplayName("submitApplication debe registrar la solicitud cuando la habilitación municipal es nueva")
    void submitApplication_success() {
        CreateMerchantApplicationDto dto = CreateMerchantApplicationDto.builder()
                .businessName("El Almacén Serrano")
                .category("GASTRONOMIA")
                .ownerName("Juan Pérez")
                .email("juan.perez@gmail.com")
                .phone("+54 3548 123456")
                .licenseNumber("HAB-2026-9988")
                .build();

        when(merchantApplicationRepository.existsByLicenseNumber("HAB-2026-9988")).thenReturn(false);

        UUID generatedId = UUID.randomUUID();
        when(merchantApplicationRepository.save(any(MerchantApplication.class))).thenAnswer(invocation -> {
            MerchantApplication app = invocation.getArgument(0);
            app.setId(generatedId);
            return app;
        });

        MerchantApplicationResponseDto response = merchantApplicationService.submitApplication(dto);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(generatedId);
        assertThat(response.getBusinessName()).isEqualTo("El Almacén Serrano");
        assertThat(response.getStatus()).isEqualTo(MerchantApplicationStatus.PENDING);
        verify(merchantApplicationRepository, times(1)).save(any(MerchantApplication.class));
    }

    @Test
    @DisplayName("submitApplication debe rechazar con ConflictException si el número de habilitación ya existe")
    void submitApplication_duplicateLicense_throwsConflict() {
        CreateMerchantApplicationDto dto = CreateMerchantApplicationDto.builder()
                .businessName("Comercio Duplicado")
                .category("COMERCIO")
                .ownerName("Ana García")
                .email("ana@gmail.com")
                .licenseNumber("HAB-EXISTENTE-01")
                .build();

        when(merchantApplicationRepository.existsByLicenseNumber("HAB-EXISTENTE-01")).thenReturn(true);

        assertThatThrownBy(() -> merchantApplicationService.submitApplication(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe una solicitud registrada con el número de habilitación");

        verify(merchantApplicationRepository, never()).save(any(MerchantApplication.class));
    }

    @Test
    @DisplayName("updateStatus debe lanzar ResourceNotFoundException si el id no existe")
    void updateStatus_notFound_throwsException() {
        UUID randomId = UUID.randomUUID();
        when(merchantApplicationRepository.findById(randomId)).thenReturn(Optional.empty());

        UpdateMerchantApplicationStatusDto dto = UpdateMerchantApplicationStatusDto.builder()
                .status(MerchantApplicationStatus.APPROVED)
                .build();

        assertThatThrownBy(() -> merchantApplicationService.updateStatus(randomId, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
