package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.review.AccommodationReviewsDto;
import com.upc.demo.dto.review.CreateReviewDto;
import com.upc.demo.dto.review.ReviewEligibilityDto;
import com.upc.demo.dto.review.ReviewResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.Review;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.entidad.enums.ReviewStatus;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;

    @Transactional
    public ReviewResponseDto create(CreateReviewDto dto) {
        String cleanCode = normalizeBookingCode(dto.getBookingCode());
        Booking booking = bookingRepository.findByBookingCode(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro ninguna reserva con el código " + cleanCode));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("No se puede calificar una reserva cancelada");
        }

        LocalDate today = LocalDate.now();
        boolean stayFinished = booking.getStatus() == BookingStatus.COMPLETED
                || !booking.getCheckOut().isAfter(today);
        if (!stayFinished) {
            throw new BadRequestException("Solo se puede calificar una estadía finalizada");
        }

        if (reviewRepository.existsByBookingCodeIgnoreCase(cleanCode)) {
            throw new ConflictException("Ya existe una reseña enviada para el código de reserva " + cleanCode);
        }

        Review review = Review.builder()
                .booking(booking)
                .accommodation(booking.getAccommodation())
                .bookingCode(cleanCode)
                .guestName(booking.getGuestName())
                .guestOrigin(resolveGuestOrigin(dto.getGuestOrigin(), booking.getGuestOrigin()))
                .stayDate(resolveStayDate(dto.getStayDate(), booking))
                .rating(dto.getRating())
                .cleanlinessRating(dto.getCleanlinessRating())
                .hostRating(dto.getHostRating())
                .locationRating(dto.getLocationRating())
                .comment(dto.getComment().trim())
                .status(ReviewStatus.PENDING)
                .build();

        return mapToResponseDto(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public ReviewEligibilityDto getEligibility(String bookingCode) {
        String cleanCode = normalizeBookingCode(bookingCode);
        Booking booking = bookingRepository.findByBookingCode(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro ninguna reserva con el código " + cleanCode));

        boolean alreadyReviewed = reviewRepository.existsByBookingCodeIgnoreCase(cleanCode);
        boolean stayFinished = booking.getStatus() == BookingStatus.COMPLETED
                || !booking.getCheckOut().isAfter(LocalDate.now());
        boolean cancelled = booking.getStatus() == BookingStatus.CANCELLED;
        boolean canReview = stayFinished && !cancelled && !alreadyReviewed;

        String reason = null;
        if (alreadyReviewed) {
            reason = "Ya enviaste una reseña para esta reserva";
        } else if (cancelled) {
            reason = "La reserva se encuentra cancelada";
        } else if (!stayFinished) {
            reason = "La estadía todavia no finalizo";
        }

        return ReviewEligibilityDto.builder()
                .bookingCode(booking.getBookingCode())
                .accommodationId(booking.getAccommodation().getId())
                .accommodationName(booking.getAccommodation().getName())
                .guestName(booking.getGuestName())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .bookingStatus(booking.getStatus())
                .alreadyReviewed(alreadyReviewed)
                .canReview(canReview)
                .reason(reason)
                .build();
    }

    @Transactional(readOnly = true)
    public AccommodationReviewsDto getApprovedByAccommodation(UUID accommodationId) {
        accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con id: " + accommodationId));

        List<Review> reviews = reviewRepository
                .findByAccommodationIdAndStatusOrderByCreatedAtDesc(accommodationId, ReviewStatus.APPROVED);

        List<ReviewResponseDto> reviewDtos = reviews.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return AccommodationReviewsDto.builder()
                .reviews(reviewDtos)
                .totalReviews((long) reviews.size())
                .averageRating(roundOneDecimal(reviews.stream().mapToInt(Review::getRating).average().orElse(0)))
                .cleanlinessAverage(roundOneDecimal(reviews.stream().mapToInt(Review::getCleanlinessRating).average().orElse(0)))
                .hostAverage(roundOneDecimal(reviews.stream().mapToInt(Review::getHostRating).average().orElse(0)))
                .locationAverage(roundOneDecimal(reviews.stream().mapToInt(Review::getLocationRating).average().orElse(0)))
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviews(ReviewStatus status) {
        List<Review> reviews = status == null
                ? reviewRepository.findAllByOrderByCreatedAtDesc()
                : reviewRepository.findByStatusOrderByCreatedAtDesc(status);

        return reviews.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDto approve(UUID id) {
        Review review = findReview(id);
        review.setStatus(ReviewStatus.APPROVED);
        review.setModerationNotes(null);
        review.setModeratedAt(LocalDateTime.now());
        return mapToResponseDto(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponseDto reject(UUID id, String notes) {
        Review review = findReview(id);
        review.setStatus(ReviewStatus.REJECTED);
        review.setModerationNotes(notes != null ? notes.trim() : null);
        review.setModeratedAt(LocalDateTime.now());
        return mapToResponseDto(reviewRepository.save(review));
    }

    private Review findReview(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("reseña no encontrada con id: " + id));
    }

    private String normalizeBookingCode(String bookingCode) {
        if (bookingCode == null || bookingCode.isBlank()) {
            throw new BadRequestException("El código de reserva es obligatorio");
        }
        return bookingCode.trim().toUpperCase();
    }

    private String resolveGuestOrigin(String requestedOrigin, String bookingOrigin) {
        if (requestedOrigin != null && !requestedOrigin.isBlank()) {
            return requestedOrigin.trim();
        }
        return bookingOrigin;
    }

    private String resolveStayDate(String requestedStayDate, Booking booking) {
        if (requestedStayDate != null && !requestedStayDate.isBlank()) {
            return requestedStayDate.trim();
        }
        return booking.getCheckIn().format(DATE_FORMATTER) + " al " + booking.getCheckOut().format(DATE_FORMATTER);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private ReviewResponseDto mapToResponseDto(Review review) {
        Accommodation accommodation = review.getAccommodation();
        return ReviewResponseDto.builder()
                .id(review.getId())
                .accommodationId(accommodation != null ? accommodation.getId() : null)
                .accommodationName(accommodation != null ? accommodation.getName() : null)
                .accommodationImageUrl(resolveMainImage(accommodation))
                .bookingCode(review.getBookingCode())
                .guestName(review.getGuestName())
                .guestOrigin(review.getGuestOrigin())
                .stayDate(review.getStayDate())
                .rating(review.getRating())
                .cleanlinessRating(review.getCleanlinessRating())
                .hostRating(review.getHostRating())
                .locationRating(review.getLocationRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .moderationNotes(review.getModerationNotes())
                .createdAt(review.getCreatedAt())
                .moderatedAt(review.getModeratedAt())
                .build();
    }

    private String resolveMainImage(Accommodation accommodation) {
        if (accommodation == null || accommodation.getImages() == null || accommodation.getImages().isEmpty()) {
            return null;
        }
        return accommodation.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                .map(AccommodationImage::getUrl)
                .findFirst()
                .orElse(accommodation.getImages().get(0).getUrl());
    }
}
