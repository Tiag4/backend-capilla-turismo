package com.upc.demo.repositorio;

import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByBookingCode(String bookingCode);
    Optional<Booking> findByBookingCodeAndGuestEmail(String bookingCode, String guestEmail);
    Optional<Booking> findByBookingCodeAndGuestEmailIgnoreCase(String bookingCode, String guestEmail);
    List<Booking> findByAccommodationId(UUID accommodationId);
    List<Booking> findByTouristId(UUID touristId);
    List<Booking> findByTouristIdOrderByCreatedAtDesc(UUID touristId);
    List<Booking> findByAccommodationHostIdOrderByCreatedAtDesc(UUID hostId);
    List<Booking> findByAccommodationHostIdAndStatusOrderByCreatedAtDesc(UUID hostId, BookingStatus status);
    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.accommodation.id = :accommodationId " +
           "AND b.status IN (com.upc.demo.entidad.enums.BookingStatus.PENDING, com.upc.demo.entidad.enums.BookingStatus.CONFIRMED) " +
           "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    boolean existsOverlappingBooking(
        @Param("accommodationId") UUID accommodationId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}