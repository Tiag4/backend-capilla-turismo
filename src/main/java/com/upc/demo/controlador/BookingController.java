package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.booking.BookingLookupResponseDto;
import com.upc.demo.dto.booking.BookingResponseDto;
import com.upc.demo.dto.booking.CreateBookingDto;
import com.upc.demo.dto.booking.UpdateBookingStatusDto;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.servicio.BookingService;
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
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody CreateBookingDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        BookingResponseDto response = bookingService.createBooking(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingStatusDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        BookingResponseDto response = bookingService.updateStatus(id, dto.getStatus(), currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser.getId(), status));
    }

    @GetMapping("/tourist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDto>> getTouristBookings(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(bookingService.getTouristBookings(currentUser.getId()));
    }

    @GetMapping("/lookup")
    public ResponseEntity<BookingLookupResponseDto> lookupBooking(
            @RequestParam String code,
            @RequestParam String email) {
        return ResponseEntity.ok(bookingService.lookup(code, email));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        return ResponseEntity.ok(bookingService.getById(id, currentUser.getId(), isAdmin));
    }
}
