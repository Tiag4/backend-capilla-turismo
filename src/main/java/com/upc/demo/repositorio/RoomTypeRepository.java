package com.upc.demo.repositorio;

import com.upc.demo.entidad.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, UUID> {

    List<RoomType> findByAccommodationIdOrderByNameAsc(UUID accommodationId);

    List<RoomType> findByAccommodationIdAndIsActiveTrueOrderByNameAsc(UUID accommodationId);

    long countByAccommodationId(UUID accommodationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomType r WHERE r.id = :id")
    Optional<RoomType> findByIdForUpdate(@Param("id") UUID id);
}
