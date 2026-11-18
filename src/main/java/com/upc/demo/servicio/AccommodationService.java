package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.accommodation.AccommodationImageDto;
import com.upc.demo.dto.accommodation.AccommodationResponseDto;
import com.upc.demo.dto.accommodation.CreateAccommodationDto;
import com.upc.demo.dto.accommodation.UnavailableDateRangeDto;
import com.upc.demo.dto.accommodation.UpdateAccommodationDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.BookingItem;
import com.upc.demo.entidad.DateBlock;
import com.upc.demo.entidad.RoomType;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.entidad.enums.ImageModerationStatus;
import com.upc.demo.repositorio.AccommodationImageRepository;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingItemRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.DateBlockRepository;
import com.upc.demo.repositorio.RoomTypeRepository;
import com.upc.demo.repositorio.UserRepository;
import com.upc.demo.servicio.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final BookingRepository bookingRepository;
    private final DateBlockRepository dateBlockRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingItemRepository bookingItemRepository;

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

        accommodations = accommodations.stream()
                .filter(this::isHostEnabled)
                .collect(Collectors.toList());

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
                .map(a -> AccommodationResponseDto.fromEntity(a, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getAll(LocalDate checkIn, LocalDate checkOut) {
        return getAll(null, null, null, null, null, checkIn, checkOut);
    }

    @Transactional(readOnly = true)
    public AccommodationResponseDto getById(UUID id) {
        Accommodation accommodation = findAccommodationById(id);
        return AccommodationResponseDto.fromEntity(accommodation, true);
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getMyAccommodations(UUID hostId) {
        return accommodationRepository.findByHostId(hostId)
                .stream()
                .map(AccommodationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> getByHostId(UUID hostId) {
        return accommodationRepository.findByHostId(hostId)
                .stream()
                .map(AccommodationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccommodationResponseDto create(CreateAccommodationDto dto, UUID hostId) {
        return create(dto, hostId, false);
    }

    @Transactional
    public AccommodationResponseDto create(CreateAccommodationDto dto, UUID hostId, boolean isAdmin) {
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
                .unitTitle(dto.getUnitTitle() != null ? dto.getUnitTitle().trim() : null)
                .surfaceSqm(dto.getSurfaceSqm())
                .bedroomsSummary(dto.getBedroomsSummary() != null ? dto.getBedroomsSummary().trim() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .checkInTime(dto.getCheckInTime() != null && !dto.getCheckInTime().isBlank() ? dto.getCheckInTime().trim() : "14:00 hs")
                .checkOutTime(dto.getCheckOutTime() != null && !dto.getCheckOutTime().isBlank() ? dto.getCheckOutTime().trim() : "10:00 hs")
                .petPolicy(dto.getPetPolicy() != null ? dto.getPetPolicy().trim() : null)
                .cancellationPolicy(dto.getCancellationPolicy() != null ? dto.getCancellationPolicy().trim() : null)
                .licenseNumber(dto.getLicenseNumber() != null ? dto.getLicenseNumber().trim() : null)
                .responseTime(dto.getResponseTime() != null ? dto.getResponseTime().trim() : null)
                .hostingSinceYear(dto.getHostingSinceYear())
                .preparationDays(dto.getPreparationDays() != null ? dto.getPreparationDays() : 0)
                .hasBreakfast(dto.getHasBreakfast() != null ? dto.getHasBreakfast() : false)
                .breakfastType(dto.getBreakfastType() != null ? dto.getBreakfastType().trim() : null)
                .amenities(dto.getAmenities() != null ? new ArrayList<>(dto.getAmenities()) : new ArrayList<>())
                .downPaymentPercentage(dto.getDownPaymentPercentage() != null ? dto.getDownPaymentPercentage() : 50)
                .bankAccountHolder(dto.getBankAccountHolder() != null ? dto.getBankAccountHolder().trim() : null)
                .bankCbu(dto.getBankCbu() != null ? dto.getBankCbu().trim() : null)
                .bankAlias(dto.getBankAlias() != null ? dto.getBankAlias().trim() : null)
                .bankName(dto.getBankName() != null ? dto.getBankName().trim() : null)
                .isActive(true)
                .isFeatured(isAdmin && Boolean.TRUE.equals(dto.getIsFeatured()))
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
        if (dto.getUnitTitle() != null) {
            accommodation.setUnitTitle(dto.getUnitTitle().trim());
        }
        if (dto.getSurfaceSqm() != null) {
            accommodation.setSurfaceSqm(dto.getSurfaceSqm());
        }
        if (dto.getBedroomsSummary() != null) {
            accommodation.setBedroomsSummary(dto.getBedroomsSummary().trim());
        }
        if (dto.getPhone() != null) {
            accommodation.setPhone(dto.getPhone().trim());
        }
        if (dto.getCheckInTime() != null && !dto.getCheckInTime().isBlank()) {
            accommodation.setCheckInTime(dto.getCheckInTime().trim());
        }
        if (dto.getCheckOutTime() != null && !dto.getCheckOutTime().isBlank()) {
            accommodation.setCheckOutTime(dto.getCheckOutTime().trim());
        }
        if (dto.getPetPolicy() != null) {
            accommodation.setPetPolicy(dto.getPetPolicy().trim());
        }
        if (dto.getCancellationPolicy() != null) {
            accommodation.setCancellationPolicy(dto.getCancellationPolicy().trim());
        }
        if (dto.getLicenseNumber() != null) {
            accommodation.setLicenseNumber(dto.getLicenseNumber().trim());
        }
        if (dto.getResponseTime() != null) {
            accommodation.setResponseTime(dto.getResponseTime().trim());
        }
        if (dto.getHostingSinceYear() != null) {
            accommodation.setHostingSinceYear(dto.getHostingSinceYear());
        }
        if (dto.getPreparationDays() != null) {
            accommodation.setPreparationDays(dto.getPreparationDays());
        }
        if (dto.getHasBreakfast() != null) {
            accommodation.setHasBreakfast(dto.getHasBreakfast());
        }
        if (dto.getBreakfastType() != null) {
            accommodation.setBreakfastType(dto.getBreakfastType().trim());
        }
        if (dto.getAmenities() != null) {
            accommodation.setAmenities(new ArrayList<>(dto.getAmenities()));
        }
        if (dto.getIsActive() != null) {
            accommodation.setIsActive(dto.getIsActive());
        }
        if (dto.getDownPaymentPercentage() != null) {
            accommodation.setDownPaymentPercentage(dto.getDownPaymentPercentage());
        }
        if (dto.getBankAccountHolder() != null) {
            accommodation.setBankAccountHolder(dto.getBankAccountHolder().trim());
        }
        if (dto.getBankCbu() != null) {
            accommodation.setBankCbu(dto.getBankCbu().trim());
        }
        if (dto.getBankAlias() != null) {
            accommodation.setBankAlias(dto.getBankAlias().trim());
        }
        if (dto.getBankName() != null) {
            accommodation.setBankName(dto.getBankName().trim());
        }
        if (dto.getIsFeatured() != null && isAdmin) {
            accommodation.setIsFeatured(dto.getIsFeatured());
        }

        Accommodation updated = accommodationRepository.save(accommodation);
        return AccommodationResponseDto.fromEntity(updated);
    }

    @Transactional
    public AccommodationResponseDto toggleStatus(UUID id, Boolean explicitActive, UUID hostId, boolean isAdmin) {
        Accommodation accommodation = findAccommodationById(id);
        validateOwnership(accommodation, hostId, isAdmin);

        boolean newActive = explicitActive != null ? explicitActive : !Boolean.TRUE.equals(accommodation.getIsActive());
        accommodation.setIsActive(newActive);

        Accommodation updated = accommodationRepository.save(accommodation);
        log.info("✔ Estado de disponibilidad del alojamiento {} ({}) cambiado a isActive={}", updated.getId(), updated.getName(), newActive);
        return AccommodationResponseDto.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, UUID hostId, boolean isAdmin) {
        Accommodation accommodation = findAccommodationById(id);
        validateOwnership(accommodation, hostId, isAdmin);

        if (accommodation.getImages() != null && !accommodation.getImages().isEmpty()) {
            for (AccommodationImage image : accommodation.getImages()) {
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
                .status(ImageModerationStatus.PENDING)
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

        if (image.getPublicId() != null && !image.getPublicId().isBlank()) {
            try {
                storageService.delete(image.getPublicId());
            } catch (Exception e) {
                log.warn("No se pudo eliminar la imagen del almacenamiento remoto (publicId: {}): {}",
                        image.getPublicId(), e.getMessage());
            }
        }

        accommodationImageRepository.delete(image);
    }

    @Transactional
    public AccommodationResponseDto toggleFeatured(UUID id) {
        Accommodation accommodation = findAccommodationById(id);
        accommodation.setIsFeatured(!Boolean.TRUE.equals(accommodation.getIsFeatured()));
        Accommodation updated = accommodationRepository.save(accommodation);
        return AccommodationResponseDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<UnavailableDateRangeDto> getCalendarAvailability(UUID accommodationId, UUID roomTypeId) {
        Accommodation accommodation = findAccommodationById(accommodationId);

        List<UnavailableDateRangeDto> unavailableRanges = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Bloqueos manuales del anfitrión (DateBlocks)
        List<DateBlock> allBlocks = dateBlockRepository.findByAccommodationIdOrderByStartDateAsc(accommodationId);
        for (DateBlock block : allBlocks) {
            boolean matchesRoom = (roomTypeId == null && block.getRoomType() == null)
                    || (roomTypeId != null && (block.getRoomType() == null || block.getRoomType().getId().equals(roomTypeId)));

            if (matchesRoom && !block.getEndDate().isBefore(today)) {
                unavailableRanges.add(UnavailableDateRangeDto.builder()
                        .id("block-" + block.getId())
                        .accommodationId(accommodationId)
                        .roomTypeId(block.getRoomType() != null ? block.getRoomType().getId() : null)
                        .startDate(block.getStartDate())
                        .endDate(block.getEndDate())
                        .reason(block.getReason() != null ? block.getReason().name() : "MAINTENANCE")
                        .type("DATE_BLOCK")
                        .notes(block.getNotes())
                        .build());
            }
        }

        int prepDays = accommodation.getPreparationDays() != null ? accommodation.getPreparationDays() : 0;
        boolean hasRoomTypes = roomTypeRepository.countByAccommodationId(accommodationId) > 0;

        // 2. Reservas de huéspedes
        if (!hasRoomTypes) {
            // Unidad única: cada reserva activa ocupa la propiedad por completo
            List<Booking> activeBookings = bookingRepository.findActiveBookingsByAccommodation(accommodationId, today);
            for (Booking booking : activeBookings) {
                unavailableRanges.add(UnavailableDateRangeDto.builder()
                        .id("booking-" + booking.getId())
                        .accommodationId(accommodationId)
                        .startDate(booking.getCheckIn())
                        .endDate(booking.getCheckOut())
                        .reason("BOOKED")
                        .type("BOOKING")
                        .build());

                // Si hay colchón de limpieza/preparación configurado, se agrega el período bloqueado subsiguiente
                if (prepDays > 0) {
                    unavailableRanges.add(UnavailableDateRangeDto.builder()
                            .id("prep-" + booking.getId())
                            .accommodationId(accommodationId)
                            .startDate(booking.getCheckOut())
                            .endDate(booking.getCheckOut().plusDays(prepDays))
                            .reason("PREPARATION")
                            .type("PREPARATION")
                            .notes("Período de desinfección y preparación posterior a estadía")
                            .build());
                }
            }
        } else {
            // Multi-habitación (Hoteles / Complejos con tipologías):
            if (roomTypeId != null) {
                RoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
                if (roomType != null && roomType.getAccommodation().getId().equals(accommodationId)) {
                    int totalUnits = roomType.getTotalUnits() != null ? roomType.getTotalUnits() : 1;
                    List<BookingItem> items = bookingItemRepository.findActiveItemsByAccommodation(accommodationId, today);
                    List<BookingItem> roomItems = items.stream()
                            .filter(i -> i.getRoomType() != null && i.getRoomType().getId().equals(roomTypeId))
                            .collect(Collectors.toList());

                    if (totalUnits == 1) {
                        for (BookingItem item : roomItems) {
                            Booking b = item.getBooking();
                            if (b != null) {
                                unavailableRanges.add(UnavailableDateRangeDto.builder()
                                        .id("booking-item-" + item.getId())
                                        .accommodationId(accommodationId)
                                        .roomTypeId(roomTypeId)
                                        .startDate(b.getCheckIn())
                                        .endDate(b.getCheckOut())
                                        .reason("BOOKED")
                                        .type("BOOKING")
                                        .build());

                                if (prepDays > 0) {
                                    unavailableRanges.add(UnavailableDateRangeDto.builder()
                                            .id("prep-item-" + item.getId())
                                            .accommodationId(accommodationId)
                                            .roomTypeId(roomTypeId)
                                            .startDate(b.getCheckOut())
                                            .endDate(b.getCheckOut().plusDays(prepDays))
                                            .reason("PREPARATION")
                                            .type("PREPARATION")
                                            .notes("Período de desinfección y preparación posterior a estadía")
                                            .build());
                                }
                            }
                        }
                    } else {
                        // Allotment multi-unidad: calcular por día si se agota el cupo
                        Set<LocalDate> soldOutDates = calculateSoldOutDates(roomType, totalUnits, roomItems, allBlocks, today);
                        List<LocalDate> sortedDates = new ArrayList<>(soldOutDates);
                        Collections.sort(sortedDates);

                        int i = 0;
                        while (i < sortedDates.size()) {
                            LocalDate rangeStart = sortedDates.get(i);
                            LocalDate current = rangeStart;
                            while (i + 1 < sortedDates.size() && sortedDates.get(i + 1).equals(current.plusDays(1))) {
                                i++;
                                current = sortedDates.get(i);
                            }
                            unavailableRanges.add(UnavailableDateRangeDto.builder()
                                    .id("allotment-" + roomTypeId + "-" + rangeStart)
                                    .accommodationId(accommodationId)
                                    .roomTypeId(roomTypeId)
                                    .startDate(rangeStart)
                                    .endDate(current.plusDays(1))
                                    .reason("BOOKED")
                                    .type("BOOKING")
                                    .notes("Cupo total agotado para esta categoría")
                                    .build());
                            i++;
                        }
                    }
                }
            }
        }

        return unavailableRanges;
    }

    private Set<LocalDate> calculateSoldOutDates(RoomType roomType, int totalUnits, List<BookingItem> items, List<DateBlock> blocks, LocalDate fromDate) {
        Set<LocalDate> soldOut = new HashSet<>();
        LocalDate maxDate = fromDate.plusMonths(6);

        for (LocalDate date = fromDate; !date.isAfter(maxDate); date = date.plusDays(1)) {
            LocalDate d = date;
            long bookedUnits = items.stream()
                    .filter(i -> i.getBooking() != null
                            && !d.isBefore(i.getBooking().getCheckIn())
                            && d.isBefore(i.getBooking().getCheckOut()))
                    .mapToLong(BookingItem::getQuantity)
                    .sum();

            long blockedUnits = blocks.stream()
                    .filter(b -> (b.getRoomType() == null || b.getRoomType().getId().equals(roomType.getId()))
                            && !d.isBefore(b.getStartDate())
                            && d.isBefore(b.getEndDate()))
                    .mapToLong(DateBlock::getUnitsBlocked)
                    .sum();

            if (bookedUnits + blockedUnits >= totalUnits) {
                soldOut.add(date);
            }
        }
        return soldOut;
    }

    private boolean isHostEnabled(Accommodation accommodation) {
        return accommodation.getHost() != null && Boolean.TRUE.equals(accommodation.getHost().getIsActive());
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