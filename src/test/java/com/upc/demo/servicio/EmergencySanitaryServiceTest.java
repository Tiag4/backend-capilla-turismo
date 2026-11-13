package com.upc.demo.servicio;

import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.sanitary.CreateOrUpdatePharmacyDto;
import com.upc.demo.dto.sanitary.EmergencySanitaryConfigDto;
import com.upc.demo.dto.sanitary.SetPharmacyOnDutyDto;
import com.upc.demo.dto.sanitary.UpdateHealthGuardNoticeDto;
import com.upc.demo.entidad.HealthGuardNotice;
import com.upc.demo.entidad.Pharmacy;
import com.upc.demo.repositorio.HealthGuardNoticeRepository;
import com.upc.demo.repositorio.PharmacyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencySanitaryServiceTest {

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private HealthGuardNoticeRepository healthGuardNoticeRepository;

    @InjectMocks
    private EmergencySanitaryService emergencySanitaryService;

    @Test
    @DisplayName("getConfig retorna lista de farmacias y datos de la guardia medica")
    void getConfig_success() {
        UUID p1Id = UUID.randomUUID();
        Pharmacy p1 = Pharmacy.builder()
                .id(p1Id)
                .name("Farmacia Pasteur")
                .address("Diagonal Buenos Aires 45")
                .phone("+54 3548 48-1250")
                .dutyHours("Guardia 24 Horas")
                .isOnDutyToday(true)
                .build();

        HealthGuardNotice guard = HealthGuardNotice.builder()
                .id(UUID.randomUUID())
                .facilityName("Hospital Municipal Dr. Amancio Rodríguez")
                .emergencyPhone("107")
                .directPhone("+54 3548 481232")
                .address("Deán Funes 530")
                .activeGuards(List.of("Guardia 24 Horas"))
                .updatedAt(LocalDateTime.now())
                .build();

        when(pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc()).thenReturn(List.of(p1));
        when(healthGuardNoticeRepository.findAll()).thenReturn(List.of(guard));

        EmergencySanitaryConfigDto config = emergencySanitaryService.getConfig();

        assertNotNull(config);
        assertEquals(1, config.getPharmacies().size());
        assertEquals("Farmacia Pasteur", config.getPharmacies().get(0).getName());
        assertTrue(config.getPharmacies().get(0).isOnDutyToday());
        assertEquals("Hospital Municipal Dr. Amancio Rodríguez", config.getHealthGuard().getFacilityName());
    }

    @Test
    @DisplayName("setPharmacyOnDuty desmarca las demas y establece la indicada como de turno")
    void setPharmacyOnDuty_success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Pharmacy p1 = Pharmacy.builder()
                .id(id1)
                .name("Farmacia Pasteur")
                .dutyHours("Guardia 24 Horas")
                .isOnDutyToday(true)
                .build();

        Pharmacy p2 = Pharmacy.builder()
                .id(id2)
                .name("Farmacia Capilla")
                .dutyHours("08:30 a 21:00 hs")
                .isOnDutyToday(false)
                .build();

        when(pharmacyRepository.findById(id2)).thenReturn(Optional.of(p2));
        when(pharmacyRepository.findAll()).thenReturn(List.of(p1, p2));
        when(pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc()).thenReturn(List.of(p2, p1));
        when(healthGuardNoticeRepository.findAll()).thenReturn(List.of());

        SetPharmacyOnDutyDto dto = SetPharmacyOnDutyDto.builder()
                .validFrom("2026-11-13")
                .validTo("2026-11-20")
                .build();

        EmergencySanitaryConfigDto result = emergencySanitaryService.setPharmacyOnDuty(id2, dto);

        assertNotNull(result);
        assertFalse(p1.isOnDutyToday());
        assertTrue(p2.isOnDutyToday());
        assertEquals("2026-11-13", p2.getValidFrom());
        assertEquals("2026-11-20", p2.getValidTo());
        verify(pharmacyRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("setPharmacyOnDuty lanza ResourceNotFoundException si la farmacia no existe")
    void setPharmacyOnDuty_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(pharmacyRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                emergencySanitaryService.setPharmacyOnDuty(id, null));
    }

    @Test
    @DisplayName("savePharmacy crea una nueva farmacia cuando no tiene ID")
    void savePharmacy_createNew_success() {
        CreateOrUpdatePharmacyDto dto = CreateOrUpdatePharmacyDto.builder()
                .name("Farmacia Nueva")
                .address("Calle Principal 100")
                .phone("+54 3548 123456")
                .dutyHours("09:00 a 20:00 hs")
                .isOnDutyToday(false)
                .build();

        when(pharmacyRepository.save(any(Pharmacy.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc()).thenReturn(List.of());
        when(healthGuardNoticeRepository.findAll()).thenReturn(List.of());

        EmergencySanitaryConfigDto result = emergencySanitaryService.savePharmacy(dto);

        assertNotNull(result);
        verify(pharmacyRepository, times(1)).save(any(Pharmacy.class));
    }

    @Test
    @DisplayName("deletePharmacy elimina la farmacia y reasigna turno si era la activa")
    void deletePharmacy_success() {
        UUID id = UUID.randomUUID();
        Pharmacy p = Pharmacy.builder()
                .id(id)
                .name("Farmacia Pasteur")
                .isOnDutyToday(true)
                .build();

        UUID remainingId = UUID.randomUUID();
        Pharmacy remaining = Pharmacy.builder()
                .id(remainingId)
                .name("Farmacia Capilla")
                .isOnDutyToday(false)
                .build();

        when(pharmacyRepository.findById(id)).thenReturn(Optional.of(p));
        when(pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc())
                .thenReturn(new ArrayList<>(List.of(remaining)));
        when(healthGuardNoticeRepository.findAll()).thenReturn(List.of());

        EmergencySanitaryConfigDto result = emergencySanitaryService.deletePharmacy(id);

        assertNotNull(result);
        verify(pharmacyRepository, times(1)).delete(p);
        assertTrue(remaining.isOnDutyToday());
        verify(pharmacyRepository, times(1)).save(remaining);
    }

    @Test
    @DisplayName("updateHealthGuardNotice actualiza los campos de guardia hospitalaria")
    void updateHealthGuardNotice_success() {
        HealthGuardNotice existing = HealthGuardNotice.builder()
                .id(UUID.randomUUID())
                .facilityName("Hospital Municipal")
                .emergencyPhone("107")
                .directPhone("03548")
                .address("Calle 1")
                .activeGuards(new ArrayList<>())
                .updatedAt(LocalDateTime.now())
                .build();

        when(healthGuardNoticeRepository.findAll()).thenReturn(List.of(existing));
        when(pharmacyRepository.findAllByOrderByIsOnDutyTodayDescNameAsc()).thenReturn(List.of());

        UpdateHealthGuardNoticeDto dto = UpdateHealthGuardNoticeDto.builder()
                .facilityName("Hospital Municipal Actualizado")
                .emergencyPhone("107 Directo")
                .specialNotice("Aviso urgente de suero")
                .build();

        EmergencySanitaryConfigDto result = emergencySanitaryService.updateHealthGuardNotice(dto);

        assertNotNull(result);
        assertEquals("Hospital Municipal Actualizado", existing.getFacilityName());
        assertEquals("107 Directo", existing.getEmergencyPhone());
        assertEquals("Aviso urgente de suero", existing.getSpecialNotice());
        verify(healthGuardNoticeRepository, times(1)).save(existing);
    }
}
