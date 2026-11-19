package com.upc.demo.servicio;

import com.upc.demo.entidad.CircuitConfig;
import com.upc.demo.repositorio.CircuitConfigRepository;
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
class CircuitConfigServiceTest {

    @Mock
    private CircuitConfigRepository circuitConfigRepository;

    @InjectMocks
    private CircuitConfigService circuitConfigService;

    @Test
    @DisplayName("getCircuitsConfigJson devuelve {} cuando no existe configuración previa")
    void getCircuitsConfigJson_empty_returnsEmptyObject() {
        when(circuitConfigRepository.findByConfigKey("main_circuits_config")).thenReturn(Optional.empty());

        String result = circuitConfigService.getCircuitsConfigJson();

        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("updateCircuitsConfigJson crea o actualiza la configuración y retorna el JSON guardado")
    void updateCircuitsConfigJson_savesAndReturnsJson() {
        String payload = "{\"mysticSpots\":[{\"id\":\"cerro-uritorco\",\"title\":\"Cerro Uritorco\"}]}";
        when(circuitConfigRepository.findByConfigKey("main_circuits_config")).thenReturn(Optional.empty());
        when(circuitConfigRepository.save(any(CircuitConfig.class))).thenAnswer(inv -> {
            CircuitConfig entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        String result = circuitConfigService.updateCircuitsConfigJson(payload);

        assertThat(result).isEqualTo(payload);
        verify(circuitConfigRepository).save(any(CircuitConfig.class));
    }
}
