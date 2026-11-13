package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.season.CreateSeasonDto;
import com.upc.demo.dto.season.TourismSeasonResponseDto;
import com.upc.demo.dto.season.UpdateSeasonDto;
import com.upc.demo.entidad.TourismSeason;
import com.upc.demo.entidad.enums.SeasonType;
import com.upc.demo.repositorio.TourismSeasonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourismSeasonServiceTest {

    @Mock
    private TourismSeasonRepository tourismSeasonRepository;

    @InjectMocks
    private TourismSeasonService tourismSeasonService;

    @Test
    @DisplayName("getAllSeasons con activeOnly false retorna todas las temporadas")
    void getAllSeasons_all() {
        TourismSeason s1 = TourismSeason.builder()
                .id(UUID.randomUUID())
                .name("Verano")
                .type(SeasonType.HIGH)
                .startDate(LocalDate.of(2027, 1, 1))
                .endDate(LocalDate.of(2027, 2, 28))
                .suggestedMultiplier(1.4)
                .isActive(true)
                .build();

        TourismSeason s2 = TourismSeason.builder()
                .id(UUID.randomUUID())
                .name("Otoño")
                .type(SeasonType.LOW)
                .startDate(LocalDate.of(2027, 4, 1))
                .endDate(LocalDate.of(2027, 6, 20))
                .suggestedMultiplier(1.0)
                .isActive(false)
                .build();

        when(tourismSeasonRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(s1, s2));

        List<TourismSeasonResponseDto> result = tourismSeasonService.getAllSeasons(false);

        assertEquals(2, result.size());
        assertEquals("Verano", result.get(0).getName());
        assertEquals("Otoño", result.get(1).getName());
    }

    @Test
    @DisplayName("getAllSeasons con activeOnly true retorna solo activas")
    void getAllSeasons_activeOnly() {
        TourismSeason s1 = TourismSeason.builder()
                .id(UUID.randomUUID())
                .name("Verano")
                .type(SeasonType.HIGH)
                .startDate(LocalDate.of(2027, 1, 1))
                .endDate(LocalDate.of(2027, 2, 28))
                .suggestedMultiplier(1.4)
                .isActive(true)
                .build();

        when(tourismSeasonRepository.findByIsActiveTrueOrderByStartDateAsc()).thenReturn(List.of(s1));

        List<TourismSeasonResponseDto> result = tourismSeasonService.getAllSeasons(true);

        assertEquals(1, result.size());
        assertEquals("Verano", result.get(0).getName());
        assertTrue(result.get(0).isActive());
    }

    @Test
    @DisplayName("getSeasonById retorna la temporada si existe")
    void getSeasonById_success() {
        UUID id = UUID.randomUUID();
        TourismSeason s = TourismSeason.builder()
                .id(id)
                .name("Fiesta OVNI")
                .type(SeasonType.SPECIAL_EVENT)
                .startDate(LocalDate.of(2027, 2, 12))
                .endDate(LocalDate.of(2027, 2, 16))
                .suggestedMultiplier(1.6)
                .isActive(true)
                .build();

        when(tourismSeasonRepository.findById(id)).thenReturn(Optional.of(s));

        TourismSeasonResponseDto result = tourismSeasonService.getSeasonById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Fiesta OVNI", result.getName());
    }

    @Test
    @DisplayName("getSeasonById lanza ResourceNotFoundException si no existe")
    void getSeasonById_notFound() {
        UUID id = UUID.randomUUID();
        when(tourismSeasonRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourismSeasonService.getSeasonById(id));
    }

    @Test
    @DisplayName("createSeason crea la temporada exitosamente")
    void createSeason_success() {
        CreateSeasonDto dto = CreateSeasonDto.builder()
                .name("Semana Santa")
                .type(SeasonType.HIGH)
                .startDate(LocalDate.of(2027, 3, 25))
                .endDate(LocalDate.of(2027, 3, 29))
                .suggestedMultiplier(1.5)
                .description("Turismo religioso")
                .build();

        TourismSeason saved = TourismSeason.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .type(dto.getType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .suggestedMultiplier(dto.getSuggestedMultiplier())
                .description(dto.getDescription())
                .isActive(true)
                .build();

        when(tourismSeasonRepository.save(any(TourismSeason.class))).thenReturn(saved);

        TourismSeasonResponseDto result = tourismSeasonService.createSeason(dto);

        assertNotNull(result);
        assertEquals("Semana Santa", result.getName());
        assertTrue(result.isActive());
        verify(tourismSeasonRepository).save(any(TourismSeason.class));
    }

    @Test
    @DisplayName("createSeason lanza BadRequestException si endDate es anterior a startDate")
    void createSeason_invalidDates() {
        CreateSeasonDto dto = CreateSeasonDto.builder()
                .name("Temporada Invalida")
                .type(SeasonType.MEDIUM)
                .startDate(LocalDate.of(2027, 5, 10))
                .endDate(LocalDate.of(2027, 5, 1))
                .suggestedMultiplier(1.2)
                .build();

        assertThrows(BadRequestException.class, () -> tourismSeasonService.createSeason(dto));
        verify(tourismSeasonRepository, never()).save(any(TourismSeason.class));
    }

    @Test
    @DisplayName("updateSeason actualiza y valida fechas correctamente")
    void updateSeason_success() {
        UUID id = UUID.randomUUID();
        TourismSeason existing = TourismSeason.builder()
                .id(id)
                .name("Verano Original")
                .type(SeasonType.HIGH)
                .startDate(LocalDate.of(2027, 1, 1))
                .endDate(LocalDate.of(2027, 2, 28))
                .suggestedMultiplier(1.4)
                .isActive(true)
                .build();

        UpdateSeasonDto updateDto = UpdateSeasonDto.builder()
                .name("Verano Modificado")
                .suggestedMultiplier(1.45)
                .build();

        when(tourismSeasonRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tourismSeasonRepository.save(any(TourismSeason.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TourismSeasonResponseDto result = tourismSeasonService.updateSeason(id, updateDto);

        assertNotNull(result);
        assertEquals("Verano Modificado", result.getName());
        assertEquals(1.45, result.getSuggestedMultiplier());
    }

    @Test
    @DisplayName("updateSeason lanza BadRequestException si fechas resultantes son invalidas")
    void updateSeason_invalidDates() {
        UUID id = UUID.randomUUID();
        TourismSeason existing = TourismSeason.builder()
                .id(id)
                .name("Invierno")
                .type(SeasonType.MEDIUM)
                .startDate(LocalDate.of(2027, 7, 1))
                .endDate(LocalDate.of(2027, 7, 20))
                .suggestedMultiplier(1.2)
                .isActive(true)
                .build();

        UpdateSeasonDto updateDto = UpdateSeasonDto.builder()
                .endDate(LocalDate.of(2027, 6, 15)) // antes del startDate original
                .build();

        when(tourismSeasonRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> tourismSeasonService.updateSeason(id, updateDto));
        verify(tourismSeasonRepository, never()).save(any(TourismSeason.class));
    }

    @Test
    @DisplayName("toggleActive invierte el estado booleano de la temporada")
    void toggleActive_success() {
        UUID id = UUID.randomUUID();
        TourismSeason season = TourismSeason.builder()
                .id(id)
                .name("Baja")
                .isActive(true)
                .build();

        when(tourismSeasonRepository.findById(id)).thenReturn(Optional.of(season));
        when(tourismSeasonRepository.save(any(TourismSeason.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TourismSeasonResponseDto result = tourismSeasonService.toggleActive(id);

        assertFalse(result.isActive());
        verify(tourismSeasonRepository).save(season);
    }

    @Test
    @DisplayName("deleteSeason elimina la temporada existente")
    void deleteSeason_success() {
        UUID id = UUID.randomUUID();
        TourismSeason season = TourismSeason.builder()
                .id(id)
                .name("Baja")
                .build();

        when(tourismSeasonRepository.findById(id)).thenReturn(Optional.of(season));

        tourismSeasonService.deleteSeason(id);

        verify(tourismSeasonRepository).delete(season);
    }
}
