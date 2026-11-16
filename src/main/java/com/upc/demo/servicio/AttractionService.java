package com.upc.demo.servicio;

import com.upc.demo.dto.attraction.AttractionImageDto;
import com.upc.demo.dto.attraction.AttractionResponseDto;
import com.upc.demo.dto.attraction.CreateAttractionDto;
import com.upc.demo.dto.attraction.UpdateAttractionDto;
import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.AttractionImage;
import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.entidad.enums.AttractionStatus;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.repositorio.AttractionImageRepository;
import com.upc.demo.repositorio.AttractionRepository;
import com.upc.demo.servicio.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final AttractionImageRepository attractionImageRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<AttractionResponseDto> getAll(AttractionCategory category,
                                             String difficulty,
                                             Boolean requiresGuide,
                                             String search) {
        Specification<Attraction> spec = (root, query, cb) -> cb.conjunction();

        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }

        if (difficulty != null && !difficulty.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("difficulty")), difficulty.toUpperCase().trim()));
        }

        if (requiresGuide != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("requiresGuide"), requiresGuide));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            ));
        }

        return attractionRepository.findAll(spec).stream()
                .map(AttractionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttractionResponseDto getById(UUID id) {
        Attraction attraction = findAttractionById(id);
        return AttractionResponseDto.fromEntity(attraction);
    }

    @Transactional
    public AttractionResponseDto create(CreateAttractionDto dto) {
        Attraction attraction = Attraction.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .difficulty(dto.getDifficulty())
                .estimatedDuration(dto.getEstimatedDuration())
                .howToGet(dto.getHowToGet())
                .requiresGuide(dto.getRequiresGuide() != null ? dto.getRequiresGuide() : false)
                .admissionFee(dto.getAdmissionFee())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .status(dto.getStatus() != null ? dto.getStatus() : com.upc.demo.entidad.enums.AttractionStatus.OPEN)
                .statusNotice(dto.getStatusNotice())
                .isFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false)
                .elevationGainMeters(dto.getElevationGainMeters())
                .maxElevationMsnm(dto.getMaxElevationMsnm())
                .distanceKm(dto.getDistanceKm())
                .trailType(dto.getTrailType())
                .terrainType(dto.getTerrainType())
                .openingHours(dto.getOpeningHours())
                .cutoffTime(dto.getCutoffTime())
                .hydrationRequirement(dto.getHydrationRequirement())
                .highlights(dto.getHighlights() != null ? new ArrayList<>(dto.getHighlights()) : new ArrayList<>())
                .recommendedGear(dto.getRecommendedGear() != null ? new ArrayList<>(dto.getRecommendedGear()) : new ArrayList<>())
                .safetyWarnings(dto.getSafetyWarnings() != null ? new ArrayList<>(dto.getSafetyWarnings()) : new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (AttractionImageDto imgDto : dto.getImages()) {
                AttractionImage image = AttractionImage.builder()
                        .url(imgDto.getUrl().trim())
                        .publicId(imgDto.getPublicId())
                        .attraction(attraction)
                        .build();
                attraction.getImages().add(image);
            }
        }

        Attraction saved = attractionRepository.save(attraction);
        return AttractionResponseDto.fromEntity(saved);
    }

    @Transactional
    public AttractionResponseDto update(UUID id, UpdateAttractionDto dto) {
        Attraction attraction = findAttractionById(id);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            attraction.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            attraction.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            attraction.setCategory(dto.getCategory());
        }
        if (dto.getDifficulty() != null) {
            attraction.setDifficulty(dto.getDifficulty());
        }
        if (dto.getEstimatedDuration() != null) {
            attraction.setEstimatedDuration(dto.getEstimatedDuration());
        }
        if (dto.getHowToGet() != null) {
            attraction.setHowToGet(dto.getHowToGet());
        }
        if (dto.getRequiresGuide() != null) {
            attraction.setRequiresGuide(dto.getRequiresGuide());
        }
        if (dto.getAdmissionFee() != null) {
            attraction.setAdmissionFee(dto.getAdmissionFee());
        }
        if (dto.getLatitude() != null) {
            attraction.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            attraction.setLongitude(dto.getLongitude());
        }
        if (dto.getStatus() != null) {
            attraction.setStatus(dto.getStatus());
        }
        if (dto.getStatusNotice() != null) {
            attraction.setStatusNotice(dto.getStatusNotice());
        }
        if (dto.getIsFeatured() != null) {
            attraction.setIsFeatured(dto.getIsFeatured());
        }
        if (dto.getElevationGainMeters() != null) {
            attraction.setElevationGainMeters(dto.getElevationGainMeters());
        }
        if (dto.getMaxElevationMsnm() != null) {
            attraction.setMaxElevationMsnm(dto.getMaxElevationMsnm());
        }
        if (dto.getDistanceKm() != null) {
            attraction.setDistanceKm(dto.getDistanceKm());
        }
        if (dto.getTrailType() != null) {
            attraction.setTrailType(dto.getTrailType());
        }
        if (dto.getTerrainType() != null) {
            attraction.setTerrainType(dto.getTerrainType());
        }
        if (dto.getOpeningHours() != null) {
            attraction.setOpeningHours(dto.getOpeningHours());
        }
        if (dto.getCutoffTime() != null) {
            attraction.setCutoffTime(dto.getCutoffTime());
        }
        if (dto.getHydrationRequirement() != null) {
            attraction.setHydrationRequirement(dto.getHydrationRequirement());
        }
        if (dto.getHighlights() != null) {
            attraction.setHighlights(new ArrayList<>(dto.getHighlights()));
        }
        if (dto.getRecommendedGear() != null) {
            attraction.setRecommendedGear(new ArrayList<>(dto.getRecommendedGear()));
        }
        if (dto.getSafetyWarnings() != null) {
            attraction.setSafetyWarnings(new ArrayList<>(dto.getSafetyWarnings()));
        }

        Attraction updated = attractionRepository.save(attraction);
        return AttractionResponseDto.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Attraction attraction = findAttractionById(id);

        if (attraction.getImages() != null && !attraction.getImages().isEmpty()) {
            for (AttractionImage image : attraction.getImages()) {
                if (image.getPublicId() != null && !image.getPublicId().isBlank()) {
                    try {
                        storageService.delete(image.getPublicId());
                    } catch (Exception e) {
                        log.warn("No se pudo eliminar la imagen del almacenamiento remoto (publicId: {}): {}",
                                image.getPublicId(), e.getMessage());
                    }
                }
            }
        }

        attractionRepository.delete(attraction);
    }

    @Transactional
    public AttractionImageDto addImage(UUID id, AttractionImageDto dto) {
        Attraction attraction = findAttractionById(id);

        AttractionImage image = AttractionImage.builder()
                .url(dto.getUrl().trim())
                .publicId(dto.getPublicId())
                .attraction(attraction)
                .build();

        AttractionImage savedImage = attractionImageRepository.save(image);
        return AttractionImageDto.fromEntity(savedImage);
    }

    @Transactional
    public void deleteImage(UUID id, UUID imageId) {
        // Validate attraction exists
        findAttractionById(id);

        AttractionImage image = attractionImageRepository.findByIdAndAttractionId(imageId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imageId + " para el paseo especificado"));

        if (image.getPublicId() != null && !image.getPublicId().isBlank()) {
            try {
                storageService.delete(image.getPublicId());
            } catch (Exception e) {
                log.warn("No se pudo eliminar la imagen del almacenamiento remoto (publicId: {}): {}",
                        image.getPublicId(), e.getMessage());
            }
        }

        attractionImageRepository.delete(image);
    }

    private Attraction findAttractionById(UUID id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atractivo turistico no encontrado con ID: " + id));
    }
}
