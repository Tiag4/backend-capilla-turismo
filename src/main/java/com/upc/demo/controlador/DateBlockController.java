package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.calendar.CreateDateBlockDto;
import com.upc.demo.dto.calendar.DateBlockResponseDto;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.servicio.DateBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DateBlockController {

    private final DateBlockService dateBlockService;

    @PostMapping("/accommodations/{accommodationId}/date-blocks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DateBlockResponseDto> createDateBlock(
            @PathVariable UUID accommodationId,
            @Valid @RequestBody CreateDateBlockDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dateBlockService.create(accommodationId, dto, currentUser.getId(), isAdmin));
    }

    @GetMapping("/accommodations/{accommodationId}/date-blocks")
    public ResponseEntity<List<DateBlockResponseDto>> listDateBlocks(@PathVariable UUID accommodationId) {
        return ResponseEntity.ok(dateBlockService.listByAccommodation(accommodationId));
    }

    @DeleteMapping("/date-blocks/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDateBlock(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        dateBlockService.delete(id, currentUser.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
