package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                                                 String search,
                                                 LocalDate checkIn,
                                                 LocalDate checkOut) {
        List<Accommodation> accommodations;

        if (checkIn != null && checkOut != null) {
            if (!checkOut.isAfter(checkIn)) {
                throw new BadRequestException("La fecha de check-out debe ser posterior a la de check-in");
            }
            accommodations = accommodationRepository.findAvailableAccommodations(checkIn, checkOut);
        } else if (checkIn != null || checkOut != null) {
            throw new BadRequestException("Debe proporcionar tanto checkIn como checkOut para filtrar por disponibilidad");
        } else {
            accommodations = accommodationRepository.findByIsActiveTrue();
        }

        if (type != null) {
            accommodations = accommodations.stream()
                    .filter(a -> a.getType() == type)
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            accommodations = accommodations.stream()
                    .filter(a -> a.getPricePerNight() != null && a.getPricePerNight().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            accommodations = accommodations.stream()
                    .filter(a -> a.getPricePerNight() != null && a.getPricePerNight().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        if (guests != null) {
            accommodations = accommodations.stream()
                    .filter(a -> a.getMaxGuests() != null && a.getMaxGuests() >= guests)
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase().trim();
            accommodations = accommodations.stream()
                    .filter(a -> (a.getName() != null && a.getName().toLowerCase().contains(lowerSearch)) ||
                            (a.getDescription() != null && a.getDescription().toLowerCase().contains(lowerSearch)) ||
                            (a.getAddress() != null && a.getAddress().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }

        return accommodations.stream()
                .map(AccommodationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getAll(LocalDate checkIn, LocalDate checkOut) {
        return getAll(null, null, null, null, null, checkIn, checkOut);
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