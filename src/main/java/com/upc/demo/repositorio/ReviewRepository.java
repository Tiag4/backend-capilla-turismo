package com.upc.demo.repositorio;

import com.upc.demo.entidad.Review;
import com.upc.demo.entidad.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByBookingId(UUID bookingId);
    Optional<Review> findByBookingCodeIgnoreCase(String bookingCode);
    boolean existsByBookingCodeIgnoreCase(String bookingCode);
    List<Review> findByAccommodationIdAndStatusOrderByCreatedAtDesc(UUID accommodationId, ReviewStatus status);
    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);
    List<Review> findAllByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodation.id = :accommodationId AND r.status = :status")
    Double findAverageRatingByAccommodationIdAndStatus(UUID accommodationId, ReviewStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM Review r WHERE r.accommodation.id = :accommodationId AND r.status = :status")
    Long countByAccommodationIdAndStatus(UUID accommodationId, ReviewStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r.accommodation.id, AVG(r.rating), COUNT(r) FROM Review r WHERE r.status = :status GROUP BY r.accommodation.id")
    List<Object[]> findReviewStatsGroupedByAccommodation(ReviewStatus status);
}
