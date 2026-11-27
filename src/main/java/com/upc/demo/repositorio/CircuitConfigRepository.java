package com.upc.demo.repositorio;

import com.upc.demo.entidad.CircuitConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircuitConfigRepository extends JpaRepository<CircuitConfig, UUID> {

    Optional<CircuitConfig> findByConfigKey(String configKey);
}
