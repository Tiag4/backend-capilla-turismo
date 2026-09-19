package com.upc.demo.controlador;

import com.upc.demo.dto.accommodation.AccommodationResponseDto;
import com.upc.demo.servicio.AccommodationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(accommodationService.getAll(checkIn, checkOut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccommodationResponseDto> getAccommodationById(@PathVariable UUID id) {
        return ResponseEntity.ok(accommodationService.getById(id));
    }
}
