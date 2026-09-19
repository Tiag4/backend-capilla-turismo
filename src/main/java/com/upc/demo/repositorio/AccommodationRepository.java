package com.upc.demo.repositorio;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.enums.AccommodationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, UUID>, JpaSpecificationExecutor<Accommodation> {
    List<Accommodation> findByHostId(UUID hostId);
    List<Accommodation> findByIsActiveTrue();
    List<Accommodation> findByType(AccommodationType type);
}