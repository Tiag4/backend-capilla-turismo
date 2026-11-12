package com.upc.demo.controlador;

import com.upc.demo.dto.accommodation.AccommodationResponseDto;
import com.upc.demo.dto.user.UserResponseDto;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.servicio.AccommodationService;
import com.upc.demo.servicio.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;
    private final AccommodationService accommodationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userService.getAllUsers(role, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam boolean isActive) {
        return ResponseEntity.ok(userService.updateUserStatus(id, isActive));
    }

    @GetMapping("/{id}/accommodations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccommodationResponseDto>> getHostAccommodations(@PathVariable UUID id) {
        return ResponseEntity.ok(accommodationService.getByHostId(id));
    }
}