package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.accommodation.AccommodationResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.repositorio.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getAll(LocalDate checkIn, LocalDate checkOut) {
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

        return accommodations.stream()
                .map(AccommodationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationResponseDto getById(UUID id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con id: " + id));
        return AccommodationResponseDto.fromEntity(accommodation);
    }
}
