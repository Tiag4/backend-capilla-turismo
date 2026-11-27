package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.season.CreateSeasonDto;
import com.upc.demo.dto.season.TourismSeasonResponseDto;
import com.upc.demo.dto.season.UpdateSeasonDto;
import com.upc.demo.entidad.TourismSeason;
import com.upc.demo.repositorio.TourismSeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourismSeasonService {

    private final TourismSeasonRepository tourismSeasonRepository;

    @Transactional(readOnly = true)
    public List<TourismSeasonResponseDto> getAllSeasons(Boolean activeOnly) {
        List<TourismSeason> seasons = Boolean.TRUE.equals(activeOnly)
                ? tourismSeasonRepository.findByIsActiveTrueOrderByStartDateAsc()
                : tourismSeasonRepository.findAllByOrderByStartDateAsc();

        return seasons.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourismSeasonResponseDto getSeasonById(UUID id) {
        TourismSeason season = tourismSeasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temporada no encontrada con ID: " + id));
        return mapToResponse(season);
    }

    @Transactional
    public TourismSeasonResponseDto createSeason(CreateSeasonDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        TourismSeason season = TourismSeason.builder()
                .name(dto.getName().trim())
                .type(dto.getType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .suggestedMultiplier(dto.getSuggestedMultiplier())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .isActive(true)
                .build();

        TourismSeason saved = tourismSeasonRepository.save(season);
        log.info("Temporada turística creada con éxito: '{}' (ID: {})", saved.getName(), saved.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public TourismSeasonResponseDto updateSeason(UUID id, UpdateSeasonDto dto) {
        TourismSeason season = tourismSeasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temporada no encontrada con ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            season.setName(dto.getName().trim());
        }
        if (dto.getType() != null) {
            season.setType(dto.getType());
        }
        if (dto.getStartDate() != null) {
            season.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            season.setEndDate(dto.getEndDate());
        }
        if (season.getEndDate().isBefore(season.getStartDate())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (dto.getSuggestedMultiplier() != null) {
            season.setSuggestedMultiplier(dto.getSuggestedMultiplier());
        }
        if (dto.getDescription() != null) {
            season.setDescription(dto.getDescription().trim());
        }
        if (dto.getIsActive() != null) {
            season.setActive(dto.getIsActive());
        }

        TourismSeason saved = tourismSeasonRepository.save(season);
        log.info("Temporada turística actualizada: '{}' (ID: {})", saved.getName(), saved.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public TourismSeasonResponseDto toggleActive(UUID id) {
        TourismSeason season = tourismSeasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temporada no encontrada con ID: " + id));

        season.setActive(!season.isActive());
        TourismSeason saved = tourismSeasonRepository.save(season);
        log.info("Estado de temporada '{}' cambiado a isActive={}", saved.getName(), saved.isActive());

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteSeason(UUID id) {
        TourismSeason season = tourismSeasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Temporada no encontrada con ID: " + id));

        tourismSeasonRepository.delete(season);
        log.info("Temporada turística eliminada: '{}' (ID: {})", season.getName(), id);
    }

    private TourismSeasonResponseDto mapToResponse(TourismSeason season) {
        return TourismSeasonResponseDto.builder()
                .id(season.getId())
                .name(season.getName())
                .type(season.getType())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .suggestedMultiplier(season.getSuggestedMultiplier())
                .description(season.getDescription())
                .isActive(season.isActive())
                .build();
    }
}
