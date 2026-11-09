package com.upc.demo.repositorio;

import com.upc.demo.entidad.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    List<BookingItem> findByBookingId(UUID bookingId);

    @Query("SELECT COALESCE(SUM(bi.quantity), 0) FROM BookingItem bi " +
           "WHERE bi.roomType.id = :roomTypeId " +
           "AND bi.booking.status IN (com.upc.demo.entidad.enums.BookingStatus.PENDING, com.upc.demo.entidad.enums.BookingStatus.CONFIRMED) " +
           "AND bi.booking.checkIn < :checkOut AND bi.booking.checkOut > :checkIn")
    long sumBookedUnits(
        @Param("roomTypeId") UUID roomTypeId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT bi.roomType.id, COALESCE(SUM(bi.quantity), 0) FROM BookingItem bi " +
           "WHERE bi.roomType.id IN :roomTypeIds " +
           "AND bi.booking.status IN (com.upc.demo.entidad.enums.BookingStatus.PENDING, com.upc.demo.entidad.enums.BookingStatus.CONFIRMED) " +
           "AND bi.booking.checkIn < :checkOut AND bi.booking.checkOut > :checkIn " +
           "GROUP BY bi.roomType.id")
    List<Object[]> sumBookedUnitsByRoomTypeIds(
        @Param("roomTypeIds") List<UUID> roomTypeIds,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT COUNT(bi) > 0 FROM BookingItem bi " +
           "WHERE bi.roomType.id = :roomTypeId " +
           "AND bi.booking.status IN (com.upc.demo.entidad.enums.BookingStatus.PENDING, com.upc.demo.entidad.enums.BookingStatus.CONFIRMED)")
    boolean existsActiveBookingsByRoomTypeId(@Param("roomTypeId") UUID roomTypeId);

    boolean existsByRatePlanId(UUID ratePlanId);
}
