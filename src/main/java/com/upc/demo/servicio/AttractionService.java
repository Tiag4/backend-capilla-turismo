package com.upc.demo.servicio;

import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.attraction.AttractionImageDto;
import com.upc.demo.dto.attraction.AttractionResponseDto;
import com.upc.demo.dto.attraction.CreateAttractionDto;
import com.upc.demo.dto.attraction.UpdateAttractionDto;
import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.AttractionImage;
import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.repositorio.AttractionImageRepository;
import com.upc.demo.repositorio.AttractionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final AttractionImageRepository attractionImageRepository;

    @Transactional(readOnly = true)
    public List<AttractionResponseDto> getAll(AttractionCategory category,
                                             String difficulty,
                                             Boolean requiresGuide,
                                             String search) {
        Specification<Attraction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (difficulty != null && !difficulty.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("difficulty")), difficulty.toLowerCase().trim()));
            }

            if (requiresGuide != null) {
                predicates.add(cb.equal(root.get("requiresGuide"), requiresGuide));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return attractionRepository.findAll(spec)
                .stream()
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

        Attraction updated = attractionRepository.save(attraction);
        return AttractionResponseDto.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Attraction attraction = findAttractionById(id);
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

        attractionImageRepository.delete(image);
    }

    private Attraction findAttractionById(UUID id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atractivo turistico no encontrado con ID: " + id));
    }
}