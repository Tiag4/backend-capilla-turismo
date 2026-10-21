package com.upc.demo.controlador;

import com.upc.demo.dto.merchant.CreateMerchantApplicationDto;
import com.upc.demo.dto.merchant.MerchantApplicationResponseDto;
import com.upc.demo.dto.merchant.UpdateMerchantApplicationStatusDto;
import com.upc.demo.entidad.enums.MerchantApplicationStatus;
import com.upc.demo.servicio.MerchantApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(name = "Adhesión de Comerciantes", description = "Endpoints para la postulación y revisión de comercios adheridos")
public class MerchantApplicationController {

    private final MerchantApplicationService merchantApplicationService;

    @PostMapping("/apply")
    @Operation(summary = "Presentar solicitud de adhesión comercial (Público)")
    public ResponseEntity<MerchantApplicationResponseDto> submitApplication(
            @Valid @RequestBody CreateMerchantApplicationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantApplicationService.submitApplication(dto));
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las solicitudes de adhesión comercial (Solo Administrador)")
    public ResponseEntity<List<MerchantApplicationResponseDto>> getAllApplications(
            @RequestParam(required = false) MerchantApplicationStatus status) {
        return ResponseEntity.ok(merchantApplicationService.getAllApplications(status));
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener detalle de una solicitud de comercio por ID (Solo Administrador)")
    public ResponseEntity<MerchantApplicationResponseDto> getApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(merchantApplicationService.getApplicationById(id));
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar estado de una solicitud comercial (Aprobada / Rechazada)")
    public ResponseEntity<MerchantApplicationResponseDto> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMerchantApplicationStatusDto dto) {
        return ResponseEntity.ok(merchantApplicationService.updateStatus(id, dto));
    }
}
