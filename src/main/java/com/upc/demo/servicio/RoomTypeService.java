package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.accommodation.CreateRatePlanDto;
import com.upc.demo.dto.accommodation.CreateRoomTypeDto;
import com.upc.demo.dto.accommodation.RatePlanResponseDto;
import com.upc.demo.dto.accommodation.RoomTypeAvailabilityDto;
import com.upc.demo.dto.accommodation.RoomTypeResponseDto;
import com.upc.demo.dto.accommodation.UpdateRoomTypeDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.RatePlan;
import com.upc.demo.entidad.RoomType;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingItemRepository;
import com.upc.demo.repositorio.RatePlanRepository;
import com.upc.demo.repositorio.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final BookingItemRepository bookingItemRepository;
    private final AccommodationRepository accommodationRepository;

    @Transactional(readOnly = true)
    public List<RoomTypeResponseDto> listByAccommodation(UUID accommodationId) {
        findAccommodation(accommodationId);
        return roomTypeRepository.findByAccommodationIdOrderByNameAsc(accommodationId).stream()
                .map(RoomTypeResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomTypeAvailabilityDto> getAvailability(UUID accommodationId, LocalDate checkIn, LocalDate checkOut) {
        findAccommodation(accommodationId);

        if ((checkIn == null) != (checkOut == null)) {
            throw new BadRequestException("Debe proporcionar tanto check-in como check-out para consultar la disponibilidad");
        }
        if (checkIn != null && !checkOut.isAfter(checkIn)) {
            throw new BadRequestException("La fecha de check-out debe ser posterior a la de check-in");
        }

        List<RoomType> roomTypes = roomTypeRepository
                .findByAccommodationIdAndIsActiveTrueOrderByNameAsc(accommodationId);

        Map<UUID, Long> bookedUnits = new HashMap<>();
        if (checkIn != null && !roomTypes.isEmpty()) {
            List<UUID> roomTypeIds = roomTypes.stream()
                    .map(RoomType::getId)
                    .collect(Collectors.toList());

            for (Object[] row : bookingItemRepository.sumBookedUnitsByRoomTypeIds(roomTypeIds, checkIn, checkOut)) {
                bookedUnits.put((UUID) row[0], ((Number) row[1]).longValue());
            }
        }

        return roomTypes.stream()
                .map(roomType -> {
                    int totalUnits = roomType.getTotalUnits() != null ? roomType.getTotalUnits() : 0;
                    long reserved = bookedUnits.getOrDefault(roomType.getId(), 0L);
                    int availableUnits = (int) Math.max(0L, totalUnits - reserved);
                    return RoomTypeAvailabilityDto.fromEntity(roomType, availableUnits);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomTypeResponseDto create(UUID accommodationId, CreateRoomTypeDto dto, UUID currentUserId, boolean isAdmin) {
        Accommodation accommodation = findAccommodation(accommodationId);
        validateOwnership(accommodation, currentUserId, isAdmin);

        if (dto.getTotalUnits() == null) {
            throw new BadRequestException("La cantidad de unidades es obligatoria");
        }
        if (dto.getBasePricePerNight() == null) {
            throw new BadRequestException("El precio base por noche es obligatorio");
        }

        int baseCapacity = dto.getBaseCapacity() != null ? dto.getBaseCapacity() : 1;
        int maxCapacity = dto.getMaxCapacity() != null ? dto.getMaxCapacity() : 2;
        validateCapacities(baseCapacity, maxCapacity);

        RoomType roomType = RoomType.builder()
                .accommodation(accommodation)
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .description(dto.getDescription())
                .baseCapacity(baseCapacity)
                .maxCapacity(maxCapacity)
                .beds(dto.getBeds() != null ? dto.getBeds() : 1)
                .totalUnits(dto.getTotalUnits())
                .basePricePerNight(dto.getBasePricePerNight())
                .isActive(true)
                .amenities(dto.getAmenities() != null ? new ArrayList<>(dto.getAmenities()) : new ArrayList<>())
                .ratePlans(new ArrayList<>())
                .build();

        return RoomTypeResponseDto.fromEntity(roomTypeRepository.save(roomType));
    }

    @Transactional
    public RoomTypeResponseDto update(UUID accommodationId, UUID roomTypeId, UpdateRoomTypeDto dto,
                                      UUID currentUserId, boolean isAdmin) {
        Accommodation accommodation = findAccommodation(accommodationId);
        validateOwnership(accommodation, currentUserId, isAdmin);
        RoomType roomType = findRoomType(accommodationId, roomTypeId);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            roomType.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            roomType.setDescription(dto.getDescription());
        }
        if (dto.getBaseCapacity() != null) {
            roomType.setBaseCapacity(dto.getBaseCapacity());
        }
        if (dto.getMaxCapacity() != null) {
            roomType.setMaxCapacity(dto.getMaxCapacity());
        }
        if (dto.getBeds() != null) {
            roomType.setBeds(dto.getBeds());
        }
        if (dto.getTotalUnits() != null) {
            roomType.setTotalUnits(dto.getTotalUnits());
        }
        if (dto.getBasePricePerNight() != null) {
            roomType.setBasePricePerNight(dto.getBasePricePerNight());
        }
        if (dto.getAmenities() != null) {
            roomType.setAmenities(new ArrayList<>(dto.getAmenities()));
        }
        if (dto.getIsActive() != null) {
            roomType.setIsActive(dto.getIsActive());
        }

        validateCapacities(roomType.getBaseCapacity(), roomType.getMaxCapacity());

        return RoomTypeResponseDto.fromEntity(roomTypeRepository.save(roomType));
    }

    @Transactional
    public void delete(UUID accommodationId, UUID roomTypeId, UUID currentUserId, boolean isAdmin) {
        Accommodation accommodation = findAccommodation(accommodationId);
        validateOwnership(accommodation, currentUserId, isAdmin);
        RoomType roomType = findRoomType(accommodationId, roomTypeId);

        if (bookingItemRepository.existsActiveBookingsByRoomTypeId(roomTypeId)) {
            throw new ConflictException("No se puede eliminar el tipo de habitación porque tiene reservas activas asociadas");
        }

        roomTypeRepository.delete(roomType);
    }

    @Transactional
    public RatePlanResponseDto createRatePlan(UUID accommodationId, UUID roomTypeId, CreateRatePlanDto dto,
                                              UUID currentUserId, boolean isAdmin) {
        Accommodation accommodation = findAccommodation(accommodationId);
        validateOwnership(accommodation, currentUserId, isAdmin);
        RoomType roomType = findRoomType(accommodationId, roomTypeId);

        if (dto.getPricePerNight() == null) {
            throw new BadRequestException("El precio por noche es obligatorio");
        }

        RatePlan ratePlan = RatePlan.builder()
                .roomType(roomType)
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .description(dto.getDescription())
                .pricePerNight(dto.getPricePerNight())
                .includesBreakfast(dto.getIncludesBreakfast() != null ? dto.getIncludesBreakfast() : false)
                .freeCancellation(dto.getFreeCancellation() != null ? dto.getFreeCancellation() : true)
                .minNights(dto.getMinNights() != null ? dto.getMinNights() : 1)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        return RatePlanResponseDto.fromEntity(ratePlanRepository.save(ratePlan));
    }

    @Transactional
    public void deleteRatePlan(UUID accommodationId, UUID roomTypeId, UUID ratePlanId,
                               UUID currentUserId, boolean isAdmin) {
        Accommodation accommodation = findAccommodation(accommodationId);
        validateOwnership(accommodation, currentUserId, isAdmin);
        findRoomType(accommodationId, roomTypeId);

        RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada con ID: " + ratePlanId));

        if (ratePlan.getRoomType() == null || !ratePlan.getRoomType().getId().equals(roomTypeId)) {
            throw new ResourceNotFoundException("Tarifa no encontrada para el tipo de habitación especificado");
        }

        if (bookingItemRepository.existsByRatePlanId(ratePlanId)) {
            throw new ConflictException("No se puede eliminar la tarifa porque está asociada a reservas existentes");
        }

        ratePlanRepository.delete(ratePlan);
    }

    private Accommodation findAccommodation(UUID accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con ID: " + accommodationId));
    }

    private RoomType findRoomType(UUID accommodationId, UUID roomTypeId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de habitación no encontrado con ID: " + roomTypeId));

        if (roomType.getAccommodation() == null || !roomType.getAccommodation().getId().equals(accommodationId)) {
            throw new ResourceNotFoundException("Tipo de habitación no encontrado para el alojamiento especificado");
        }

        return roomType;
    }

    private void validateOwnership(Accommodation accommodation, UUID currentUserId, boolean isAdmin) {
        if (!isAdmin && (accommodation.getHost() == null || !accommodation.getHost().getId().equals(currentUserId))) {
            throw new ForbiddenException("No tienes permisos para gestionar los tipos de habitación de este alojamiento");
        }
    }

    private void validateCapacities(Integer baseCapacity, Integer maxCapacity) {
        if (baseCapacity != null && maxCapacity != null && maxCapacity < baseCapacity) {
            throw new BadRequestException("La capacidad máxima no puede ser menor que la capacidad base");
        }
    }
}
