package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.calendar.CreateDateBlockDto;
import com.upc.demo.dto.calendar.DateBlockResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.DateBlock;
import com.upc.demo.entidad.enums.DateBlockReason;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.DateBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateBlockService {

    private final DateBlockRepository dateBlockRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public DateBlockResponseDto create(UUID accommodationId, CreateDateBlockDto dto, UUID userId, boolean isAdmin) {
        if (!dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new BadRequestException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con id: " + accommodationId));

        validateOwnership(accommodation, userId, isAdmin);

        if (bookingRepository.existsOverlappingBooking(accommodationId, dto.getStartDate(), dto.getEndDate())) {
            throw new ConflictException("No se puede bloquear el rango: existen reservas activas en esas fechas");
        }

        if (dateBlockRepository.existsOverlappingBlock(accommodationId, dto.getStartDate(), dto.getEndDate())) {
            throw new ConflictException("Ya existe un bloqueo de fechas que se superpone con el rango solicitado");
        }

        DateBlock block = DateBlock.builder()
                .accommodation(accommodation)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason() != null ? dto.getReason() : DateBlockReason.MAINTENANCE)
                .notes(dto.getNotes() != null ? dto.getNotes().trim() : null)
                .build();

        return mapToResponseDto(dateBlockRepository.save(block));
    }

    @Transactional(readOnly = true)
    public List<DateBlockResponseDto> listByAccommodation(UUID accommodationId) {
        accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con id: " + accommodationId));

        return dateBlockRepository.findByAccommodationIdOrderByStartDateAsc(accommodationId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(UUID id, UUID userId, boolean isAdmin) {
        DateBlock block = dateBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueo de fechas no encontrado con id: " + id));

        validateOwnership(block.getAccommodation(), userId, isAdmin);
        dateBlockRepository.delete(block);
    }

    private void validateOwnership(Accommodation accommodation, UUID userId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        UUID hostId = accommodation.getHost() != null ? accommodation.getHost().getId() : null;
        if (userId == null || hostId == null || !hostId.equals(userId)) {
            throw new ForbiddenException("No tienes permisos para gestionar bloqueos de este alojamiento");
        }
    }

    private DateBlockResponseDto mapToResponseDto(DateBlock block) {
        Accommodation accommodation = block.getAccommodation();
        return DateBlockResponseDto.builder()
                .id(block.getId())
                .accommodationId(accommodation != null ? accommodation.getId() : null)
                .accommodationName(accommodation != null ? accommodation.getName() : null)
                .startDate(block.getStartDate())
                .endDate(block.getEndDate())
                .reason(block.getReason())
                .notes(block.getNotes())
                .createdAt(block.getCreatedAt())
                .build();
    }
}
