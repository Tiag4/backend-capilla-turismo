package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.accommodation.CreateRatePlanDto;
import com.upc.demo.dto.accommodation.CreateRoomTypeDto;
import com.upc.demo.dto.accommodation.RatePlanResponseDto;
import com.upc.demo.dto.accommodation.RoomTypeAvailabilityDto;
import com.upc.demo.dto.accommodation.RoomTypeResponseDto;
import com.upc.demo.dto.accommodation.UpdateRoomTypeDto;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.servicio.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accommodations/{accommodationId}/rooms")
@RequiredArgsConstructor
@Tag(name = "Tipos de Habitación", description = "Gestión de inventario multi-unidad y disponibilidad por tipo de habitación")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    @Operation(summary = "Listar tipos de habitación con disponibilidad (público)")
    public ResponseEntity<List<RoomTypeAvailabilityDto>> getAvailability(
            @PathVariable UUID accommodationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(roomTypeService.getAvailability(accommodationId, checkIn, checkOut));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    @Operation(summary = "Crear un tipo de habitación (Host/Admin)")
    public ResponseEntity<RoomTypeResponseDto> createRoomType(
            @PathVariable UUID accommodationId,
            @Valid @RequestBody CreateRoomTypeDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomTypeService.create(accommodationId, dto, currentUser.getId(), isAdmin));
    }

    @PutMapping("/{roomTypeId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    @Operation(summary = "Actualizar un tipo de habitación (Host/Admin)")
    public ResponseEntity<RoomTypeResponseDto> updateRoomType(
            @PathVariable UUID accommodationId,
            @PathVariable UUID roomTypeId,
            @Valid @RequestBody UpdateRoomTypeDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.ok(roomTypeService.update(accommodationId, roomTypeId, dto, currentUser.getId(), isAdmin));
    }

    @DeleteMapping("/{roomTypeId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    @Operation(summary = "Eliminar un tipo de habitación (Host/Admin)")
    public ResponseEntity<Void> deleteRoomType(
            @PathVariable UUID accommodationId,
            @PathVariable UUID roomTypeId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        roomTypeService.delete(accommodationId, roomTypeId, currentUser.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomTypeId}/rate-plans")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    @Operation(summary = "Crear una tarifa para un tipo de habitación (Host/Admin)")
    public ResponseEntity<RatePlanResponseDto> createRatePlan(
            @PathVariable UUID accommodationId,
            @PathVariable UUID roomTypeId,
            @Valid @RequestBody CreateRatePlanDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomTypeService.createRatePlan(accommodationId, roomTypeId, dto, currentUser.getId(), isAdmin));
    }

    @DeleteMapping("/{roomTypeId}/rate-plans/{ratePlanId}")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    @Operation(summary = "Eliminar una tarifa de un tipo de habitación (Host/Admin)")
    public ResponseEntity<Void> deleteRatePlan(
            @PathVariable UUID accommodationId,
            @PathVariable UUID roomTypeId,
            @PathVariable UUID ratePlanId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        roomTypeService.deleteRatePlan(accommodationId, roomTypeId, ratePlanId, currentUser.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
