package com.upc.demo.controlador;

import com.upc.demo.dto.alert.AlertResponseDto;
import com.upc.demo.dto.alert.CreateOrUpdateAlertDto;
import com.upc.demo.servicio.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/active")
    public ResponseEntity<AlertResponseDto> getActiveAlert() {
        return alertService.getActiveAlert()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AlertResponseDto>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponseDto> createAlert(@Valid @RequestBody CreateOrUpdateAlertDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponseDto> updateAlert(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrUpdateAlertDto dto) {
        return ResponseEntity.ok(alertService.updateAlert(id, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponseDto> toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean isActive) {
        return ResponseEntity.ok(alertService.toggleStatus(id, isActive));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlert(@PathVariable UUID id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
