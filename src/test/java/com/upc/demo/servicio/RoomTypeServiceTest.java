package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.accommodation.CreateRoomTypeDto;
import com.upc.demo.dto.accommodation.RoomTypeAvailabilityDto;
import com.upc.demo.dto.accommodation.RoomTypeResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.RatePlan;
import com.upc.demo.entidad.RoomType;
import com.upc.demo.entidad.User;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingItemRepository;
import com.upc.demo.repositorio.RatePlanRepository;
import com.upc.demo.repositorio.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RatePlanRepository ratePlanRepository;

    @Mock
    private BookingItemRepository bookingItemRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private RoomTypeService roomTypeService;

    @Test
    @DisplayName("listByAccommodation devuelve los tipos ordenados y mapea sus tarifas")
    void listByAccommodation_mapsRoomTypesAndRatePlans() {
        UUID accommodationId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder()
                .id(accommodationId)
                .name("Hostería Boutique Los Nogales")
                .build();

        RatePlan flexible = RatePlan.builder()
                .id(UUID.randomUUID())
                .name("Tarifa flexible")
                .pricePerNight(new BigDecimal("120000.00"))
                .includesBreakfast(false)
                .freeCancellation(true)
                .minNights(1)
                .isActive(true)
                .build();

        RatePlan desayuno = RatePlan.builder()
                .id(UUID.randomUUID())
                .name("Desayuno incluido")
                .pricePerNight(new BigDecimal("135000.00"))
                .includesBreakfast(true)
                .freeCancellation(true)
                .minNights(1)
                .isActive(true)
                .build();

        RoomType standard = RoomType.builder()
                .id(UUID.randomUUID())
                .accommodation(accommodation)
                .name("Standard Doble")
                .baseCapacity(2)
                .maxCapacity(2)
                .beds(2)
                .totalUnits(6)
                .basePricePerNight(new BigDecimal("120000.00"))
                .isActive(true)
                .ratePlans(new ArrayList<>(List.of(flexible, desayuno)))
                .build();

        RoomType superior = RoomType.builder()
                .id(UUID.randomUUID())
                .accommodation(accommodation)
                .name("Superior")
                .baseCapacity(2)
                .maxCapacity(3)
                .beds(2)
                .totalUnits(3)
                .basePricePerNight(new BigDecimal("165000.00"))
                .isActive(true)
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.findByAccommodationIdOrderByNameAsc(accommodationId))
                .thenReturn(List.of(standard, superior));

        List<RoomTypeResponseDto> result = roomTypeService.listByAccommodation(accommodationId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Standard Doble");
        assertThat(result.get(1).getName()).isEqualTo("Superior");
        assertThat(result.get(0).getRatePlans()).hasSize(2);
        assertThat(result.get(0).getRatePlans().get(0).getName()).isEqualTo("Tarifa flexible");
        assertThat(result.get(0).getRatePlans().get(1).getIncludesBreakfast()).isTrue();
        assertThat(result.get(0).getTotalUnits()).isEqualTo(6);
    }

    @Test
    @DisplayName("listByAccommodation lanza ResourceNotFoundException si el alojamiento no existe")
    void listByAccommodation_notFound_throwsException() {
        UUID accommodationId = UUID.randomUUID();
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomTypeService.listByAccommodation(accommodationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Alojamiento no encontrado");
    }

    @Test
    @DisplayName("getAvailability descuenta las unidades reservadas cuando se informan fechas")
    void getAvailability_withDates_subtractsBookedUnits() {
        UUID accommodationId = UUID.randomUUID();
        UUID roomTypeId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder().id(accommodationId).build();

        RoomType standard = RoomType.builder()
                .id(roomTypeId)
                .accommodation(accommodation)
                .name("Standard Doble")
                .totalUnits(6)
                .basePricePerNight(new BigDecimal("120000.00"))
                .isActive(true)
                .build();

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(8);

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.findByAccommodationIdAndIsActiveTrueOrderByNameAsc(accommodationId))
                .thenReturn(List.of(standard));
        when(bookingItemRepository.sumBookedUnitsByRoomTypeIds(anyList(), any(), any()))
                .thenReturn(Collections.singletonList(new Object[]{roomTypeId, 2L}));

        List<RoomTypeAvailabilityDto> result = roomTypeService.getAvailability(accommodationId, checkIn, checkOut);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalUnits()).isEqualTo(6);
        assertThat(result.get(0).getAvailableUnits()).isEqualTo(4);
    }

    @Test
    @DisplayName("getAvailability sin fechas devuelve la disponibilidad total")
    void getAvailability_withoutDates_returnsTotalUnits() {
        UUID accommodationId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder().id(accommodationId).build();

        RoomType superior = RoomType.builder()
                .id(UUID.randomUUID())
                .accommodation(accommodation)
                .name("Superior")
                .totalUnits(3)
                .basePricePerNight(new BigDecimal("165000.00"))
                .isActive(true)
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.findByAccommodationIdAndIsActiveTrueOrderByNameAsc(accommodationId))
                .thenReturn(List.of(superior));

        List<RoomTypeAvailabilityDto> result = roomTypeService.getAvailability(accommodationId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailableUnits()).isEqualTo(3);
        verifyNoInteractions(bookingItemRepository);
    }

    @Test
    @DisplayName("getAvailability lanza BadRequestException si el check-out no es posterior al check-in")
    void getAvailability_invalidDateRange_throwsException() {
        UUID accommodationId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder().id(accommodationId).build();
        LocalDate checkIn = LocalDate.now().plusDays(5);

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> roomTypeService.getAvailability(accommodationId, checkIn, checkIn))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("check-out debe ser posterior");
    }

    @Test
    @DisplayName("create lanza BadRequestException si la capacidad máxima es menor que la capacidad base")
    void create_maxCapacityLowerThanBase_throwsException() {
        UUID accommodationId = UUID.randomUUID();
        Accommodation accommodation = Accommodation.builder()
                .id(accommodationId)
                .host(User.builder().id(UUID.randomUUID()).build())
                .build();

        CreateRoomTypeDto dto = CreateRoomTypeDto.builder()
                .name("Suite")
                .baseCapacity(3)
                .maxCapacity(2)
                .beds(3)
                .totalUnits(1)
                .basePricePerNight(new BigDecimal("220000.00"))
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> roomTypeService.create(accommodationId, dto, null, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("capacidad máxima no puede ser menor");

        verify(roomTypeRepository, never()).save(any(RoomType.class));
    }
}
