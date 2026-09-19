package com.upc.demo.repositorio;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.enums.AccommodationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, UUID>, JpaSpecificationExecutor<Accommodation> {
    List<Accommodation> findByHostId(UUID hostId);
    List<Accommodation> findByIsActiveTrue();
    List<Accommodation> findByType(AccommodationType type);

    @Query("SELECT a FROM Accommodation a WHERE a.isActive = true AND a.id NOT IN (" +
           "  SELECT b.accommodation.id FROM Booking b " +
           "  WHERE b.status IN (com.upc.demo.entidad.enums.BookingStatus.PENDING, com.upc.demo.entidad.enums.BookingStatus.CONFIRMED) " +
           "  AND b.checkIn < :checkOut AND b.checkOut > :checkIn" +
           ")")
    List<Accommodation> findAvailableAccommodations(
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}