package com.upc.demo.servicio;

import com.upc.demo.entidad.PortalConfig;
import com.upc.demo.repositorio.PortalConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalConfigService {

    private static final String DEFAULT_CONFIG_KEY = "main_portal_config";
    private final PortalConfigRepository portalConfigRepository;

    @Transactional(readOnly = true)
    public String getPortalConfigJson() {
        return portalConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .map(PortalConfig::getDataJson)
                .orElse("{}");
    }

    @Transactional
    public String updatePortalConfigJson(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            configJson = "{}";
        }

        PortalConfig entity = portalConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(() -> PortalConfig.builder()
                        .configKey(DEFAULT_CONFIG_KEY)
                        .dataJson("{}")
                        .build());

        entity.setDataJson(configJson);
        PortalConfig saved = portalConfigRepository.save(entity);
        log.info("Configuración de portada del portal actualizada exitosamente (ID: {})", saved.getId());
        return saved.getDataJson();
    }
}
