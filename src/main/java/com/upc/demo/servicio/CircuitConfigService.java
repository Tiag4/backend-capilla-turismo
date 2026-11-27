package com.upc.demo.servicio;

import com.upc.demo.entidad.CircuitConfig;
import com.upc.demo.repositorio.CircuitConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitConfigService {

    private static final String DEFAULT_CONFIG_KEY = "main_circuits_config";
    private final CircuitConfigRepository circuitConfigRepository;

    @Transactional(readOnly = true)
    public String getCircuitsConfigJson() {
        return circuitConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .map(CircuitConfig::getDataJson)
                .orElse("{}");
    }

    @Transactional
    public String updateCircuitsConfigJson(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            configJson = "{}";
        }

        CircuitConfig entity = circuitConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(() -> CircuitConfig.builder()
                        .configKey(DEFAULT_CONFIG_KEY)
                        .dataJson("{}")
                        .build());

        entity.setDataJson(configJson);
        CircuitConfig saved = circuitConfigRepository.save(entity);
        log.info("Configuración de circuitos temáticos actualizada exitosamente (ID: {})", saved.getId());
        return saved.getDataJson();
    }
}
