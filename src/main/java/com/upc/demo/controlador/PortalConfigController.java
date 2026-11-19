package com.upc.demo.controlador;

import com.upc.demo.servicio.PortalConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portal-config")
@RequiredArgsConstructor
@Tag(name = "PortalConfig", description = "Endpoints de configuración dinámica de portada y secciones del Portal Turístico")
public class PortalConfigController {

    private final PortalConfigService portalConfigService;

    @Operation(summary = "Obtener configuración dinámica de portada del portal (público)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPortalConfig() {
        return ResponseEntity.ok(portalConfigService.getPortalConfigJson());
    }

    @Operation(summary = "Actualizar configuración dinámica de portada del portal (solo ADMIN)")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePortalConfig(@RequestBody String configJson) {
        return ResponseEntity.ok(portalConfigService.updatePortalConfigJson(configJson));
    }
}
