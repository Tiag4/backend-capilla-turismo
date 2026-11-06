package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.dto.review.AccommodationReviewsDto;
import com.upc.demo.dto.review.CreateReviewDto;
import com.upc.demo.dto.review.ReviewResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.Review;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.entidad.enums.ReviewStatus;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private ReviewService reviewService;

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    @DisplayName("create debe registrar la reseña en estado PENDING para una estadía completada")
    void create_completedStay_savesPendingReview() {
        UUID bookingId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder().id(accommodationId).name("Cabaña Serrana").build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .bookingCode("CAP-2026-1234")
                .accommodation(accommodation)
                .guestName("Martina Gómez")
                .guestOrigin("Córdoba")
                .checkIn(TODAY.minusDays(4))
                .checkOut(TODAY.minusDays(1))
                .status(BookingStatus.COMPLETED)
                .build();

        when(bookingRepository.findByBookingCode("CAP-2026-1234")).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingCodeIgnoreCase("CAP-2026-1234")).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(UUID.randomUUID());
            review.setCreatedAt(java.time.LocalDateTime.now());
            return review;
        });

        ReviewResponseDto response = reviewService.create(buildCreateDto("CAP-2026-1234"));

        assertThat(response.getStatus()).isEqualTo(ReviewStatus.PENDING);
        assertThat(response.getGuestName()).isEqualTo("Martina Gómez");
        assertThat(response.getBookingCode()).isEqualTo("CAP-2026-1234");
        assertThat(response.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("create debe rechazar una segunda reseña para el mismo código de reserva")
    void create_duplicateCode_throwsConflict() {
        Booking booking = completedBooking();
        when(bookingRepository.findByBookingCode("CAP-2026-1234")).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingCodeIgnoreCase("CAP-2026-1234")).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(buildCreateDto("CAP-2026-1234")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe una reseña");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("create debe rechazar la calificación cuando la estadía todavía no finalizó")
    void create_futureStay_throwsBadRequest() {
        Booking booking = completedBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCheckIn(TODAY.plusDays(2));
        booking.setCheckOut(TODAY.plusDays(5));
        when(bookingRepository.findByBookingCode("CAP-2026-1234")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> reviewService.create(buildCreateDto("CAP-2026-1234")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("estadía finalizada");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("approve debe cambiar el estado a APPROVED y registrar la fecha de moderación")
    void approve_pendingReview_updatesStatus() {
        Review review = Review.builder()
                .id(UUID.randomUUID())
                .status(ReviewStatus.PENDING)
                .accommodation(Accommodation.builder().id(UUID.randomUUID()).name("Cabaña Serrana").build())
                .build();
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponseDto response = reviewService.approve(review.getId());

        assertThat(response.getStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(response.getModeratedAt()).isNotNull();
    }

    @Test
    @DisplayName("getApprovedByAccommodation debe calcular promedios y listar solo reseñas aprobadas")
    void getApprovedByAccommodation_computesAverages() {
        UUID accommodationId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder().id(accommodationId).name("Cabaña Serrana").build();
        Review first = approvedReview(accommodation, 5, 4, 5, 3);
        Review second = approvedReview(accommodation, 3, 2, 3, 5);

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(reviewRepository.findByAccommodationIdAndStatusOrderByCreatedAtDesc(accommodationId, ReviewStatus.APPROVED))
                .thenReturn(List.of(first, second));

        AccommodationReviewsDto response = reviewService.getApprovedByAccommodation(accommodationId);

        assertThat(response.getTotalReviews()).isEqualTo(2);
        assertThat(response.getAverageRating()).isEqualTo(4.0);
        assertThat(response.getCleanlinessAverage()).isEqualTo(3.0);
        assertThat(response.getHostAverage()).isEqualTo(4.0);
        assertThat(response.getLocationAverage()).isEqualTo(4.0);
    }

    private CreateReviewDto buildCreateDto(String bookingCode) {
        return CreateReviewDto.builder()
                .bookingCode(bookingCode)
                .rating(5)
                .cleanlinessRating(4)
                .hostRating(5)
                .locationRating(5)
                .comment("Una estadía excelente, con vistas hermosas y atención impecable.")
                .build();
    }

    private Booking completedBooking() {
        Accommodation accommodation = Accommodation.builder()
                .id(UUID.randomUUID())
                .name("Cabaña Serrana")
                .build();
        return Booking.builder()
                .id(UUID.randomUUID())
                .bookingCode("CAP-2026-1234")
                .accommodation(accommodation)
                .guestName("Martina Gómez")
                .checkIn(TODAY.minusDays(4))
                .checkOut(TODAY.minusDays(1))
                .status(BookingStatus.COMPLETED)
                .build();
    }

    private Review approvedReview(Accommodation accommodation, int rating, int cleanliness, int host, int location) {
        return Review.builder()
                .id(UUID.randomUUID())
                .accommodation(accommodation)
                .status(ReviewStatus.APPROVED)
                .rating(rating)
                .cleanlinessRating(cleanliness)
                .hostRating(host)
                .locationRating(location)
                .build();
    }
}
