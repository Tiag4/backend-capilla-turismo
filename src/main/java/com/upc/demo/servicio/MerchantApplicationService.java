package com.upc.demo.servicio;

import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.merchant.CreateMerchantApplicationDto;
import com.upc.demo.dto.merchant.MerchantApplicationResponseDto;
import com.upc.demo.dto.merchant.UpdateMerchantApplicationStatusDto;
import com.upc.demo.entidad.MerchantApplication;
import com.upc.demo.entidad.enums.MerchantApplicationStatus;
import com.upc.demo.repositorio.MerchantApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplicationService {

    private final MerchantApplicationRepository merchantApplicationRepository;

    @Transactional(readOnly = true)
    public List<MerchantApplicationResponseDto> getAllApplications(MerchantApplicationStatus status) {
        List<MerchantApplication> list = (status != null)
                ? merchantApplicationRepository.findByStatusOrderByCreatedAtDesc(status)
                : merchantApplicationRepository.findAllByOrderByCreatedAtDesc();

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MerchantApplicationResponseDto getApplicationById(UUID id) {
        MerchantApplication app = merchantApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de comercio no encontrada con ID: " + id));
        return toDto(app);
    }

    @Transactional
    public MerchantApplicationResponseDto submitApplication(CreateMerchantApplicationDto dto) {
        String cleanLicense = dto.getLicenseNumber().trim();

        if (merchantApplicationRepository.existsByLicenseNumber(cleanLicense)) {
            throw new ConflictException("Ya existe una solicitud registrada con el número de habilitación: " + cleanLicense);
        }

        MerchantApplication application = MerchantApplication.builder()
                .businessName(dto.getBusinessName().trim())
                .category(dto.getCategory().trim())
                .ownerName(dto.getOwnerName().trim())
                .email(dto.getEmail().toLowerCase().trim())
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .licenseNumber(cleanLicense)
                .status(MerchantApplicationStatus.PENDING)
                .build();

        MerchantApplication saved = merchantApplicationRepository.save(application);

        // Notificación oficial por correo (o registro auditado en logs del sistema)
        sendApplicationNotificationEmail(saved);

        return toDto(saved);
    }

    @Transactional
    public MerchantApplicationResponseDto updateStatus(UUID id, UpdateMerchantApplicationStatusDto dto) {
        MerchantApplication app = merchantApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de comercio no encontrada con ID: " + id));

        app.setStatus(dto.getStatus());
        if (dto.getNotes() != null) {
            app.setNotes(dto.getNotes().trim());
        }

        MerchantApplication updated = merchantApplicationRepository.save(app);
        log.info("Solicitud comercial {} actualizada a estado {}", id, updated.getStatus());
        return toDto(updated);
    }

    private void sendApplicationNotificationEmail(MerchantApplication app) {
        log.info("📧 [NOTIFICACIÓN DE ADHESIÓN COMERCIAL]");
        log.info("Se ha recibido una nueva solicitud de adhesión para el comercio '{}'", app.getBusinessName());
        log.info("Titular: {} | Email: {} | Habilitación N°: {}", app.getOwnerName(), app.getEmail(), app.getLicenseNumber());
        log.info("Destinatario oficial: camara.comercio@capilladelmonte.gov.ar");
    }

    private MerchantApplicationResponseDto toDto(MerchantApplication entity) {
        return MerchantApplicationResponseDto.builder()
                .id(entity.getId())
                .businessName(entity.getBusinessName())
                .category(entity.getCategory())
                .ownerName(entity.getOwnerName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .licenseNumber(entity.getLicenseNumber())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
