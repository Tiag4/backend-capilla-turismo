package com.upc.demo.repositorio;

import com.upc.demo.entidad.AttractionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttractionImageRepository extends JpaRepository<AttractionImage, UUID> {
    List<AttractionImage> findByAttractionId(UUID attractionId);
}