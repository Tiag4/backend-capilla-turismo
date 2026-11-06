package com.upc.demo.controlador;

import com.upc.demo.dto.review.AccommodationReviewsDto;
import com.upc.demo.dto.review.CreateReviewDto;
import com.upc.demo.dto.review.RejectReviewDto;
import com.upc.demo.dto.review.ReviewEligibilityDto;
import com.upc.demo.dto.review.ReviewResponseDto;
import com.upc.demo.entidad.enums.ReviewStatus;
import com.upc.demo.servicio.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody CreateReviewDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(dto));
    }

    @GetMapping("/eligibility")
    public ResponseEntity<ReviewEligibilityDto> getEligibility(@RequestParam String code) {
        return ResponseEntity.ok(reviewService.getEligibility(code));
    }

    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<AccommodationReviewsDto> getApprovedByAccommodation(
            @PathVariable UUID accommodationId) {
        return ResponseEntity.ok(reviewService.getApprovedByAccommodation(accommodationId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(
            @RequestParam(required = false) ReviewStatus status) {
        return ResponseEntity.ok(reviewService.getReviews(status));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDto> approveReview(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDto> rejectReview(
            @PathVariable UUID id,
            @Valid @RequestBody RejectReviewDto dto) {
        return ResponseEntity.ok(reviewService.reject(id, dto.getNotes()));
    }
}
