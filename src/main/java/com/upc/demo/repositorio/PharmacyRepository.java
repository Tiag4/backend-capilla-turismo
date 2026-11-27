package com.upc.demo.repositorio;

import com.upc.demo.entidad.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, UUID> {
    List<Pharmacy> findAllByOrderByIsOnDutyTodayDescNameAsc();
}
