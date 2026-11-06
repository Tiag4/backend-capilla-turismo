package com.upc.demo.repositorio;

import com.upc.demo.entidad.DateBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DateBlockRepository extends JpaRepository<DateBlock, UUID> {
    List<DateBlock> findByAccommodationIdOrderByStartDateAsc(UUID accommodationId);

    @Query("SELECT COUNT(db) > 0 FROM DateBlock db WHERE db.accommodation.id = :accommodationId " +
           "AND db.startDate < :endDate AND db.endDate > :startDate")
    boolean existsOverlappingBlock(
        @Param("accommodationId") UUID accommodationId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
