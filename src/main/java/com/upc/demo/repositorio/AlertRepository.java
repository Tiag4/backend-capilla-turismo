package com.upc.demo.repositorio;

import com.upc.demo.entidad.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    Optional<Alert> findFirstByIsActiveTrueOrderByCreatedAtDesc();

    List<Alert> findAllByOrderByCreatedAtDesc();
}
