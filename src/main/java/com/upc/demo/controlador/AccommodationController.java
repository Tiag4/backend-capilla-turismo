package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.accommodation.AccommodationImageDto;
import com.upc.demo.dto.accommodation.AccommodationResponseDto;
import com.upc.demo.dto.accommodation.CreateAccommodationDto;
import com.upc.demo.dto.accommodation.UpdateAccommodationDto;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.servicio.AccommodationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ResponseEntity<List<AccommodationResponseDto>> getAllAccommodations(
            @RequestParam(required = false) AccommodationType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(accommodationService.getAll(type, minPrice, maxPrice, guests, search, checkIn, checkOut));
    }

    @GetMapping("/my-accommodations")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<List<AccommodationResponseDto>> getMyAccommodations(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(accommodationService.getMyAccommodations(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccommodationResponseDto> getAccommodationById(@PathVariable UUID id) {
        return ResponseEntity.ok(accommodationService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<AccommodationResponseDto> createAccommodation(
            @Valid @RequestBody CreateAccommodationDto dto,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accommodationService.create(dto, user.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<AccommodationResponseDto> updateAccommodation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccommodationDto dto,
            @AuthenticationPrincipal UserPrincipal user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        return ResponseEntity.ok(accommodationService.update(id, dto, user.getId(), isAdmin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccommodation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        accommodationService.delete(id, user.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<AccommodationImageDto> addImage(
            @PathVariable UUID id,
            @Valid @RequestBody AccommodationImageDto dto,
            @AuthenticationPrincipal UserPrincipal user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accommodationService.addImage(id, dto, user.getId(), isAdmin));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId,
            @AuthenticationPrincipal UserPrincipal user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        accommodationService.deleteImage(id, imageId, user.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}