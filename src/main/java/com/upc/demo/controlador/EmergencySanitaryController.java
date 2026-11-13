package com.upc.demo.controlador;

import com.upc.demo.dto.sanitary.CreateOrUpdatePharmacyDto;
import com.upc.demo.dto.sanitary.EmergencySanitaryConfigDto;
import com.upc.demo.dto.sanitary.SetPharmacyOnDutyDto;
import com.upc.demo.dto.sanitary.UpdateHealthGuardNoticeDto;
import com.upc.demo.servicio.EmergencySanitaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergency-sanitary")
@RequiredArgsConstructor
@Tag(name = "Guardia Sanitaria y Farmacias", description = "Endpoints para consulta pública y administración de farmacias de turno y hospital")
public class EmergencySanitaryController {

    private final EmergencySanitaryService emergencySanitaryService;

    @GetMapping
    @Operation(summary = "Obtener configuración sanitaria, farmacias y guardia médica activa")
    public ResponseEntity<EmergencySanitaryConfigDto> getConfig() {
        return ResponseEntity.ok(emergencySanitaryService.getConfig());
    }

    @PostMapping("/pharmacies")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear o actualizar una farmacia del ejido urbano")
    public ResponseEntity<EmergencySanitaryConfigDto> savePharmacy(
            @Valid @RequestBody CreateOrUpdatePharmacyDto dto) {
        return ResponseEntity.ok(emergencySanitaryService.savePharmacy(dto));
    }

    @PutMapping("/pharmacies/{id}/on-duty")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Designar una farmacia como de turno para el período")
    public ResponseEntity<EmergencySanitaryConfigDto> setPharmacyOnDuty(
            @PathVariable UUID id,
            @RequestBody(required = false) SetPharmacyOnDutyDto dto) {
        return ResponseEntity.ok(emergencySanitaryService.setPharmacyOnDuty(id, dto));
    }

    @DeleteMapping("/pharmacies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una farmacia registrada")
    public ResponseEntity<EmergencySanitaryConfigDto> deletePharmacy(@PathVariable UUID id) {
        return ResponseEntity.ok(emergencySanitaryService.deletePharmacy(id));
    }

    @PutMapping("/health-guard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar información y avisos de la guardia del Hospital Municipal")
    public ResponseEntity<EmergencySanitaryConfigDto> updateHealthGuard(
            @RequestBody UpdateHealthGuardNoticeDto dto) {
        return ResponseEntity.ok(emergencySanitaryService.updateHealthGuardNotice(dto));
    }
}
