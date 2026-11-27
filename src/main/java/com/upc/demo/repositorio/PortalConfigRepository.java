package com.upc.demo.repositorio;

import com.upc.demo.entidad.PortalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortalConfigRepository extends JpaRepository<PortalConfig, UUID> {

    Optional<PortalConfig> findByConfigKey(String configKey);
}
