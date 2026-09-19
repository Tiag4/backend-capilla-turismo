package com.upc.demo.repositorio;

import com.upc.demo.entidad.AccommodationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, UUID> {
    List<AccommodationImage> findByAccommodationId(UUID accommodationId);
}