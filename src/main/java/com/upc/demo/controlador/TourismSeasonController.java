package com.upc.demo.controlador;

import com.upc.demo.dto.season.CreateSeasonDto;
import com.upc.demo.dto.season.TourismSeasonResponseDto;
import com.upc.demo.dto.season.UpdateSeasonDto;
import com.upc.demo.servicio.TourismSeasonService;
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
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
@Tag(name = "Temporadas Turísticas", description = "Endpoints para consulta pública y administración de temporadas y tarifas sugeridas")
public class TourismSeasonController {

    private final TourismSeasonService tourismSeasonService;

    @GetMapping
    @Operation(summary = "Listar todas las temporadas turísticas oficiales")
    public ResponseEntity<List<TourismSeasonResponseDto>> getAllSeasons(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(tourismSeasonService.getAllSeasons(activeOnly));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una temporada por ID")
    public ResponseEntity<TourismSeasonResponseDto> getSeasonById(@PathVariable UUID id) {
        return ResponseEntity.ok(tourismSeasonService.getSeasonById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva temporada turística oficial")
    public ResponseEntity<TourismSeasonResponseDto> createSeason(@Valid @RequestBody CreateSeasonDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourismSeasonService.createSeason(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una temporada turística existente")
    public ResponseEntity<TourismSeasonResponseDto> updateSeason(
            @PathVariable UUID id,
            @RequestBody UpdateSeasonDto dto) {
        return ResponseEntity.ok(tourismSeasonService.updateSeason(id, dto));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alternar estado activo/inactivo de una temporada")
    public ResponseEntity<TourismSeasonResponseDto> toggleActive(@PathVariable UUID id) {
        return ResponseEntity.ok(tourismSeasonService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una temporada turística")
    public ResponseEntity<Void> deleteSeason(@PathVariable UUID id) {
        tourismSeasonService.deleteSeason(id);
        return ResponseEntity.noContent().build();
    }
}
