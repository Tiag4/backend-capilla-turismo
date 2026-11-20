package com.upc.demo.servicio;

import com.upc.demo.entidad.PortalConfig;
import com.upc.demo.repositorio.PortalConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalConfigServiceTest {

    @Mock
    private PortalConfigRepository portalConfigRepository;

    @InjectMocks
    private PortalConfigService portalConfigService;

    @Test
    @DisplayName("getPortalConfigJson devuelve {} cuando no existe configuración previa")
    void getPortalConfigJson_empty_returnsEmptyObject() {
        when(portalConfigRepository.findByConfigKey("main_portal_config")).thenReturn(Optional.empty());

        String result = portalConfigService.getPortalConfigJson();

        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("updatePortalConfigJson crea o actualiza la configuración y retorna el JSON guardado")
    void updatePortalConfigJson_savesAndReturnsJson() {
        String payload = "{\"heroConfig\":{\"title\":\"Capilla del Monte\"}}";
        when(portalConfigRepository.findByConfigKey("main_portal_config")).thenReturn(Optional.empty());
        when(portalConfigRepository.save(any(PortalConfig.class))).thenAnswer(inv -> {
            PortalConfig entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        String result = portalConfigService.updatePortalConfigJson(payload);

        assertThat(result).isEqualTo(payload);
        verify(portalConfigRepository).save(any(PortalConfig.class));
    }
}
