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
}
