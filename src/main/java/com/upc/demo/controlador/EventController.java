package com.upc.demo.controlador;

import com.upc.demo.dto.event.CreateOrUpdateEventDto;
import com.upc.demo.dto.event.EventResponseDto;
import com.upc.demo.entidad.enums.EventCategory;
import com.upc.demo.servicio.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Eventos Culturales", description = "Endpoints para la gestión y consulta de la agenda cultural municipal")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Listar eventos culturales (público con filtros opcionales)")
    public ResponseEntity<List<EventResponseDto>> getAllEvents(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(eventService.getAllEvents(activeOnly, category, from, to));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalles de un evento cultural por ID")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo evento cultural (Solo Administrador)")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody CreateOrUpdateEventDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un evento cultural existente (Solo Administrador)")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrUpdateEventDto dto) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un evento cultural (Solo Administrador)")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
