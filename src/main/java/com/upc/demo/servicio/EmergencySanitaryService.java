package com.upc.demo.servicio;

import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.sanitary.*;
import com.upc.demo.entidad.HealthGuardNotice;
import com.upc.demo.entidad.Pharmacy;
import com.upc.demo.repositorio.HealthGuardNoticeRepository;
import com.upc.demo.repositorio.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencySanitaryService {

    private final PharmacyRepository pharmacyRepository;
    private final HealthGuardNoticeRepository healthGuardNoticeRepository;

    @Transactional(readOnly = true)
    public EmergencySanitaryConfigDto getConfig() {
        List<PharmacyResponseDto> pharmacies = pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc()
                .stream()
                .map(this::mapToPharmacyResponse)
                .toList();

        HealthGuardNoticeDto healthGuardDto = healthGuardNoticeRepository.findAll().stream()
                .findFirst()
                .map(this::mapToHealthGuardDto)
                .orElseGet(this::createDefaultHealthGuardDto);

        return EmergencySanitaryConfigDto.builder()
                .pharmacies(pharmacies)
                .healthGuard(healthGuardDto)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Transactional
    public EmergencySanitaryConfigDto setPharmacyOnDuty(UUID pharmacyId, SetPharmacyOnDutyDto dto) {
        Pharmacy targetPharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmacia no encontrada con ID: " + pharmacyId));

        List<Pharmacy> all = pharmacyRepository.findAll();
        for (Pharmacy p : all) {
            if (p.getId().equals(pharmacyId)) {
                p.setOnDutyToday(true);
                if (dto != null) {
                    if (dto.getValidFrom() != null && !dto.getValidFrom().isBlank()) {
                        p.setValidFrom(dto.getValidFrom());
                    } else if (p.getValidFrom() == null) {
                        p.setValidFrom(LocalDate.now().toString());
                    }
                    if (dto.getValidTo() != null && !dto.getValidTo().isBlank()) {
                        p.setValidTo(dto.getValidTo());
                    }
                }
                if (!p.getDutyHours().toLowerCase().contains("24") && !p.getDutyHours().contains("De Turno")) {
                    p.setDutyHours(p.getDutyHours() + " (De Turno)");
                }
            } else {
                p.setOnDutyToday(false);
            }
        }
        pharmacyRepository.saveAll(all);
        log.info("Farmacia establecida de turno: '{}' (ID: {})", targetPharmacy.getName(), pharmacyId);

        return getConfig();
    }

    @Transactional
    public EmergencySanitaryConfigDto savePharmacy(CreateOrUpdatePharmacyDto dto) {
        Pharmacy pharmacy = null;

        if (dto.getId() != null && !dto.getId().isBlank()) {
            try {
                UUID parsedId = UUID.fromString(dto.getId());
                pharmacy = pharmacyRepository.findById(parsedId).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // Si el ID generado por el cliente no es UUID valido, se considera nueva
            }
        }

        boolean willBeOnDuty = Boolean.TRUE.equals(dto.getIsOnDutyToday());

        if (willBeOnDuty) {
            List<Pharmacy> existing = pharmacyRepository.findAll();
            existing.forEach(p -> p.setOnDutyToday(false));
            pharmacyRepository.saveAll(existing);
        }

        if (pharmacy == null) {
            pharmacy = Pharmacy.builder()
                    .name(dto.getName().trim())
                    .address(dto.getAddress().trim())
                    .phone(dto.getPhone().trim())
                    .whatsapp(dto.getWhatsapp() != null ? dto.getWhatsapp().trim() : null)
                    .dutyHours(dto.getDutyHours().trim())
                    .validFrom(dto.getValidFrom())
                    .validTo(dto.getValidTo())
                    .isOnDutyToday(willBeOnDuty)
                    .notes(dto.getNotes() != null ? dto.getNotes().trim() : null)
                    .googleMapsUrl(dto.getGoogleMapsUrl() != null ? dto.getGoogleMapsUrl().trim() : null)
                    .build();
        } else {
            pharmacy.setName(dto.getName().trim());
            pharmacy.setAddress(dto.getAddress().trim());
            pharmacy.setPhone(dto.getPhone().trim());
            pharmacy.setWhatsapp(dto.getWhatsapp() != null ? dto.getWhatsapp().trim() : null);
            pharmacy.setDutyHours(dto.getDutyHours().trim());
            pharmacy.setValidFrom(dto.getValidFrom());
            pharmacy.setValidTo(dto.getValidTo());
            pharmacy.setOnDutyToday(willBeOnDuty);
            pharmacy.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);
            pharmacy.setGoogleMapsUrl(dto.getGoogleMapsUrl() != null ? dto.getGoogleMapsUrl().trim() : null);
        }

        pharmacyRepository.save(pharmacy);
        log.info("Farmacia guardada con exito: '{}' (ID: {})", pharmacy.getName(), pharmacy.getId());

        return getConfig();
    }

    @Transactional
    public EmergencySanitaryConfigDto deletePharmacy(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmacia no encontrada con ID: " + pharmacyId));

        boolean wasOnDuty = pharmacy.isOnDutyToday();
        pharmacyRepository.delete(pharmacy);
        log.info("Farmacia eliminada con exito: '{}' (ID: {})", pharmacy.getName(), pharmacyId);

        if (wasOnDuty) {
            List<Pharmacy> remaining = pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc();
            if (!remaining.isEmpty()) {
                Pharmacy first = remaining.get(0);
                first.setOnDutyToday(true);
                pharmacyRepository.save(first);
                log.info("Nueva farmacia asignada como de turno: '{}'", first.getName());
            }
        }

        return getConfig();
    }

    @Transactional
    public EmergencySanitaryConfigDto updateHealthGuardNotice(UpdateHealthGuardNoticeDto dto) {
        HealthGuardNotice notice = healthGuardNoticeRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> HealthGuardNotice.builder()
                        .facilityName("Hospital Municipal Dr. Amancio Rodríguez")
                        .emergencyPhone("107")
                        .directPhone("+54 3548 481232")
                        .address("Deán Funes 530")
                        .updatedAt(LocalDateTime.now())
                        .build());

        if (dto.getFacilityName() != null && !dto.getFacilityName().isBlank()) {
            notice.setFacilityName(dto.getFacilityName().trim());
        }
        if (dto.getEmergencyPhone() != null && !dto.getEmergencyPhone().isBlank()) {
            notice.setEmergencyPhone(dto.getEmergencyPhone().trim());
        }
        if (dto.getDirectPhone() != null && !dto.getDirectPhone().isBlank()) {
            notice.setDirectPhone(dto.getDirectPhone().trim());
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            notice.setAddress(dto.getAddress().trim());
        }
        if (dto.getActiveGuards() != null) {
            notice.setActiveGuards(dto.getActiveGuards());
        }
        if (dto.getSpecialNotice() != null) {
            notice.setSpecialNotice(dto.getSpecialNotice().trim());
        }
        notice.setUpdatedAt(LocalDateTime.now());

        healthGuardNoticeRepository.save(notice);
        log.info("Guardia hospitalaria actualizada: '{}'", notice.getFacilityName());

        return getConfig();
    }

    private PharmacyResponseDto mapToPharmacyResponse(Pharmacy p) {
        return PharmacyResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .address(p.getAddress())
                .phone(p.getPhone())
                .whatsapp(p.getWhatsapp())
                .dutyHours(p.getDutyHours())
                .validFrom(p.getValidFrom())
                .validTo(p.getValidTo())
                .isOnDutyToday(p.isOnDutyToday())
                .notes(p.getNotes())
                .googleMapsUrl(p.getGoogleMapsUrl())
                .build();
    }

    private HealthGuardNoticeDto mapToHealthGuardDto(HealthGuardNotice h) {
        return HealthGuardNoticeDto.builder()
                .id(h.getId())
                .facilityName(h.getFacilityName())
                .emergencyPhone(h.getEmergencyPhone())
                .directPhone(h.getDirectPhone())
                .address(h.getAddress())
                .activeGuards(h.getActiveGuards())
                .specialNotice(h.getSpecialNotice())
                .updatedAt(h.getUpdatedAt())
                .build();
    }

    private HealthGuardNoticeDto createDefaultHealthGuardDto() {
        return HealthGuardNoticeDto.builder()
                .facilityName("Hospital Municipal Dr. Amancio Rodríguez")
                .emergencyPhone("107")
                .directPhone("+54 3548 481232")
                .address("Deán Funes 530")
                .activeGuards(List.of(
                        "Guardia Médica Clínica Permanente (24 Horas)",
                        "Shock Room de Traumatología y Urgencias",
                        "Provisión Inmediata de Suero Antiofídico y Antiescorpiónico",
                        "Guardia Pediátrica de Refuerzo"
                ))
                .specialNotice("Guardia médica activa las 24 horas todos los días del año. Ante mordeduras de ofidios o picaduras de alacrán, concurrir inmediatamente a la guardia.")
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
