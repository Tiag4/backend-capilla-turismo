package com.upc.demo.controlador;

import com.upc.demo.servicio.CircuitConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/circuits")
@RequiredArgsConstructor
@Tag(name = "Circuits", description = "Endpoints de configuración y paradas de Circuitos Temáticos")
public class CircuitConfigController {

    private final CircuitConfigService circuitConfigService;

    @Operation(summary = "Obtener circuitos temáticos activos (público)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCircuitsConfig() {
        return ResponseEntity.ok(circuitConfigService.getCircuitsConfigJson());
    }

    @Operation(summary = "Actualizar circuitos temáticos (solo ADMIN)")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateCircuitsConfig(@RequestBody String configJson) {
        return ResponseEntity.ok(circuitConfigService.updateCircuitsConfigJson(configJson));
    }
}
