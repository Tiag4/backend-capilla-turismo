package com.upc.demo.repositorio;

import com.upc.demo.entidad.MobilityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MobilityConfigRepository extends JpaRepository<MobilityConfig, UUID> {

    Optional<MobilityConfig> findByConfigKey(String configKey);
}
