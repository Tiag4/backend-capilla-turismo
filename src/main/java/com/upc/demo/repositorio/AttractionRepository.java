package com.upc.demo.repositorio;

import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.enums.AttractionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, UUID> {
    List<Attraction> findByCategory(AttractionCategory category);
    List<Attraction> findByDifficulty(String difficulty);
}