package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.booking.BookingResponseDto;
import com.upc.demo.dto.booking.CreateBookingDto;
import com.upc.demo.servicio.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
