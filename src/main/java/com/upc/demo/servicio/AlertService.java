package com.upc.demo.servicio;

import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.alert.AlertResponseDto;
import com.upc.demo.dto.alert.CreateOrUpdateAlertDto;
import com.upc.demo.entidad.Alert;
import com.upc.demo.repositorio.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public Optional<AlertResponseDto> getActiveAlert() {
        return alertRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .filter(alert -> alert.getExpiresAt() == null || alert.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertResponseDto createAlert(CreateOrUpdateAlertDto dto) {
        boolean active = dto.getIsActive() == null || dto.getIsActive();

        if (active) {
            deactivateAllActiveAlerts();
        }

        Alert alert = Alert.builder()
                .title(dto.getTitle().trim())
                .message(dto.getMessage().trim())
                .severity(dto.getSeverity())
                .linkUrl(dto.getLinkUrl() != null ? dto.getLinkUrl().trim() : null)
                .linkText(dto.getLinkText() != null ? dto.getLinkText().trim() : null)
                .isActive(active)
                .expiresAt(dto.getExpiresAt())
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Alerta de seguridad/meteorologica creada con ID: {}, severidad: {}", saved.getId(), saved.getSeverity());
        return toDto(saved);
    }

    @Transactional
    public AlertResponseDto updateAlert(UUID id, CreateOrUpdateAlertDto dto) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con ID: " + id));

        boolean newActiveState = dto.getIsActive() != null ? dto.getIsActive() : alert.isActive();
        if (newActiveState && !alert.isActive()) {
            deactivateAllActiveAlerts();
        }

        alert.setTitle(dto.getTitle().trim());
        alert.setMessage(dto.getMessage().trim());
        alert.setSeverity(dto.getSeverity());
        alert.setLinkUrl(dto.getLinkUrl() != null ? dto.getLinkUrl().trim() : null);
        alert.setLinkText(dto.getLinkText() != null ? dto.getLinkText().trim() : null);
        alert.setActive(newActiveState);
        alert.setExpiresAt(dto.getExpiresAt());

        Alert updated = alertRepository.save(alert);
        log.info("Alerta con ID: {} actualizada con exito", updated.getId());
        return toDto(updated);
    }

    @Transactional
    public AlertResponseDto toggleStatus(UUID id, boolean isActive) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con ID: " + id));

        if (isActive) {
            deactivateAllActiveAlerts();
        }
        alert.setActive(isActive);

        Alert updated = alertRepository.save(alert);
        log.info("Estado de alerta con ID: {} cambiado a {}", updated.getId(), isActive);
        return toDto(updated);
    }

    @Transactional
    public void deleteAlert(UUID id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerta no encontrada con ID: " + id);
        }
        alertRepository.deleteById(id);
        log.info("Alerta con ID: {} eliminada", id);
    }

    private void deactivateAllActiveAlerts() {
        List<Alert> all = alertRepository.findAll();
        for (Alert a : all) {
            if (a.isActive()) {
                a.setActive(false);
            }
        }
        alertRepository.saveAll(all);
    }

    private AlertResponseDto toDto(Alert alert) {
        return AlertResponseDto.builder()
                .id(alert.getId())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .severity(alert.getSeverity())
                .linkUrl(alert.getLinkUrl())
                .linkText(alert.getLinkText())
                .isActive(alert.isActive())
                .expiresAt(alert.getExpiresAt())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}
