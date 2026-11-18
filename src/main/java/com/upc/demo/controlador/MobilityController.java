package com.upc.demo.controlador;

import com.upc.demo.servicio.MobilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mobility")
@RequiredArgsConstructor
@Tag(name = "Mobility", description = "Endpoints de configuración y tarifas de movilidad y accesos")
public class MobilityController {

    private final MobilityService mobilityService;

    @Operation(summary = "Obtener configuración dinámica de movilidad (público)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMobilityConfig() {
        return ResponseEntity.ok(mobilityService.getMobilityConfigJson());
    }

    @Operation(summary = "Actualizar configuración dinámica de movilidad (solo ADMIN)")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateMobilityConfig(@RequestBody String configJson) {
        return ResponseEntity.ok(mobilityService.updateMobilityConfigJson(configJson));
    }
}
