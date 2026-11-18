package com.upc.demo.servicio;

import com.upc.demo.entidad.MobilityConfig;
import com.upc.demo.repositorio.MobilityConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobilityService {

    private static final String DEFAULT_CONFIG_KEY = "main_mobility_config";
    private final MobilityConfigRepository mobilityConfigRepository;

    @Transactional(readOnly = true)
    public String getMobilityConfigJson() {
        return mobilityConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .map(MobilityConfig::getDataJson)
                .orElse("{}");
    }

    @Transactional
    public String updateMobilityConfigJson(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            configJson = "{}";
        }

        MobilityConfig entity = mobilityConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(() -> MobilityConfig.builder()
                        .configKey(DEFAULT_CONFIG_KEY)
                        .dataJson("{}")
                        .build());

        entity.setDataJson(configJson);
        MobilityConfig saved = mobilityConfigRepository.save(entity);
        log.info("Configuración de movilidad actualizada exitosamente (ID: {})", saved.getId());
        return saved.getDataJson();
    }
}
