package com.upc.demo.repositorio;

import com.upc.demo.entidad.HealthGuardNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HealthGuardNoticeRepository extends JpaRepository<HealthGuardNotice, UUID> {
}
