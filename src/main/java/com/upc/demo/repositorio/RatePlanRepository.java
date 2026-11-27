package com.upc.demo.repositorio;

import com.upc.demo.entidad.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, UUID> {

    List<RatePlan> findByRoomTypeIdOrderByPricePerNightAsc(UUID roomTypeId);

    List<RatePlan> findByRoomTypeIdAndIsActiveTrueOrderByPricePerNightAsc(UUID roomTypeId);
}
