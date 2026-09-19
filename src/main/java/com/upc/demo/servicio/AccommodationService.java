package com.upc.demo.servicio;

import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.accommodation.AccommodationImageDto;
import com.upc.demo.dto.accommodation.AccommodationResponseDto;
import com.upc.demo.dto.accommodation.CreateAccommodationDto;
import com.upc.demo.dto.accommodation.UpdateAccommodationDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.repositorio.AccommodationImageRepository;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getAll(AccommodationType type,
                                                 BigDecimal minPrice,
                                                 BigDecimal maxPrice,
                                                 Integer guests,
                                                 String search) {
        Specification<Accommodation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Por defecto, solo alojamientos activos en el listado publico
            predicates.add(cb.isTrue(root.get("isActive")));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerNight"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), maxPrice));
            }

            if (guests != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), guests));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate addressMatch = cb.like(cb.lower(root.get("address")), pattern);
                predicates.add(cb.or(nameMatch, descMatch, addressMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return accommodationRepository.findAll(spec)
                .stream()
                .map(AccommodationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationResponseDto getById(UUID id) {
        Accommodation accommodation = findAccommodationById(id);
        return AccommodationResponseDto.fromEntity(accommodation);
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getMyAccommodations(UUID hostId) {
        return accommodationRepository.findByHostId(hostId)
                .stream()
                .map(AccommodationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccommodationResponseDto create(CreateAccommodationDto dto, UUID hostId) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host no encontrado con ID: " + hostId));

        Accommodation accommodation = Accommodation.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .type(dto.getType())
                .address(dto.getAddress().trim())
                .locality(dto.getLocality() != null && !dto.getLocality().isBlank() ? dto.getLocality().trim() : "Capilla del Monte")
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .pricePerNight(dto.getPricePerNight())
                .maxGuests(dto.getMaxGuests())
                .amenities(dto.getAmenities() != null ? new ArrayList<>(dto.getAmenities()) : new ArrayList<>())
                .isActive(true)
                .host(host)
                .images(new ArrayList<>())
                .build();

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (AccommodationImageDto imgDto : dto.getImages()) {
                AccommodationImage image = AccommodationImage.builder()
                        .url(imgDto.getUrl().trim())
                        .publicId(imgDto.getPublicId())
                        .isMain(imgDto.getIsMain() != null ? imgDto.getIsMain() : false)
                        .accommodation(accommodation)
                        .build();
                accommodation.getImages().add(image);
            }
        }

        Accommodation saved = accommodationRepository.save(accommodation);
        return AccommodationResponseDto.fromEntity(saved);
    }

    @Transactional
    public AccommodationResponseDto update(UUID id, UpdateAccommodationDto dto, UUID hostId, boolean isAdmin) {
        Accommodation accommodation = findAccommodationById(id);
        validateOwnership(accommodation, hostId, isAdmin);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            accommodation.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            accommodation.setDescription(dto.getDescription());
        }
        if (dto.getType() != null) {
            accommodation.setType(dto.getType());
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            accommodation.setAddress(dto.getAddress().trim());
        }
        if (dto.getLocality() != null && !dto.getLocality().isBlank()) {
            accommodation.setLocality(dto.getLocality().trim());
        }
        if (dto.getLatitude() != null) {
            accommodation.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            accommodation.setLongitude(dto.getLongitude());
        }
        if (dto.getPricePerNight() != null) {
            accommodation.setPricePerNight(dto.getPricePerNight());
        }
        if (dto.getMaxGuests() != null) {
            accommodation.setMaxGuests(dto.getMaxGuests());
        }
        if (dto.getAmenities() != null) {
            accommodation.setAmenities(new ArrayList<>(dto.getAmenities()));
        }
        if (dto.getIsActive() != null) {
            accommodation.setIsActive(dto.getIsActive());
        }

        Accommodation updated = accommodationRepository.save(accommodation);
        return AccommodationResponseDto.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, UUID hostId, boolean isAdmin) {
        Accommodation accommodation = findAccommodationById(id);
        validateOwnership(accommodation, hostId, isAdmin);
        accommodationRepository.delete(accommodation);
    }

    @Transactional
    public AccommodationImageDto addImage(UUID id, AccommodationImageDto dto, UUID hostId, boolean isAdmin) {
        Accommodation accommodation = findAccommodationById(id);
        validateOwnership(accommodation, hostId, isAdmin);

        boolean isMain = Boolean.TRUE.equals(dto.getIsMain());
        if (isMain && accommodation.getImages() != null) {
            for (AccommodationImage existingImg : accommodation.getImages()) {
                existingImg.setIsMain(false);
                accommodationImageRepository.save(existingImg);
            }
        }

        AccommodationImage image = AccommodationImage.builder()
                .url(dto.getUrl().trim())
                .publicId(dto.getPublicId())
                .isMain(isMain)
                .accommodation(accommodation)
                .build();

        AccommodationImage savedImage = accommodationImageRepository.save(image);
        return AccommodationImageDto.fromEntity(savedImage);
    }

    @Transactional
    public void deleteImage(UUID id, UUID imageId, UUID hostId, boolean isAdmin) {
        Accommodation accommodation = findAccommodationById(id);
        validateOwnership(accommodation, hostId, isAdmin);

        AccommodationImage image = accommodationImageRepository.findByIdAndAccommodationId(imageId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imageId + " para el alojamiento especificado"));

        accommodationImageRepository.delete(image);
    }

    private Accommodation findAccommodationById(UUID id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con ID: " + id));
    }

    private void validateOwnership(Accommodation accommodation, UUID hostId, boolean isAdmin) {
        if (!isAdmin && (accommodation.getHost() == null || !accommodation.getHost().getId().equals(hostId))) {
            throw new ForbiddenException("No tienes permisos para modificar o gestionar este alojamiento");
        }
    }
}