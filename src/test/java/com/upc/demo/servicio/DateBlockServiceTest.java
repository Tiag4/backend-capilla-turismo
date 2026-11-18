package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.dto.calendar.CreateDateBlockDto;
import com.upc.demo.dto.calendar.DateBlockResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.DateBlock;
import com.upc.demo.entidad.RoomType;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.DateBlockReason;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingItemRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.DateBlockRepository;
import com.upc.demo.repositorio.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DateBlockServiceTest {

    @Mock
    private DateBlockRepository dateBlockRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingItemRepository bookingItemRepository;

    @InjectMocks
    private DateBlockService dateBlockService;

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    @DisplayName("create debe guardar el bloqueo global cuando el rango está libre")
    void create_freeRange_savesBlock() {
        UUID hostId = UUID.randomUUID();
        Accommodation accommodation = accommodation(hostId);
        CreateDateBlockDto dto = dto(TODAY.plusDays(5), TODAY.plusDays(10));

        when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(dateBlockRepository.existsOverlappingGlobalBlock(any(), any(), any())).thenReturn(false);
        when(dateBlockRepository.save(any(DateBlock.class))).thenAnswer(invocation -> {
            DateBlock block = invocation.getArgument(0);
            block.setId(UUID.randomUUID());
            block.setCreatedAt(java.time.LocalDateTime.now());
            return block;
        });

        DateBlockResponseDto response = dateBlockService.create(accommodation.getId(), dto, hostId, false);

        assertThat(response.getStartDate()).isEqualTo(dto.getStartDate());
        assertThat(response.getEndDate()).isEqualTo(dto.getEndDate());
        assertThat(response.getReason()).isEqualTo(DateBlockReason.MAINTENANCE);
        verify(dateBlockRepository).save(any(DateBlock.class));
    }

    @Test
    @DisplayName("create debe rechazar un rango con reservas activas")
    void create_withActiveBooking_throwsConflict() {
        UUID hostId = UUID.randomUUID();
        Accommodation accommodation = accommodation(hostId);
        when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> dateBlockService.create(
                accommodation.getId(), dto(TODAY.plusDays(2), TODAY.plusDays(4)), hostId, false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("reservas activas");

        verify(dateBlockRepository, never()).save(any(DateBlock.class));
    }

    @Test
    @DisplayName("create debe rechazar un rango superpuesto con otro bloqueo general")
    void create_withExistingBlock_throwsConflict() {
        UUID hostId = UUID.randomUUID();
        Accommodation accommodation = accommodation(hostId);
        when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(dateBlockRepository.existsOverlappingGlobalBlock(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> dateBlockService.create(
                accommodation.getId(), dto(TODAY.plusDays(2), TODAY.plusDays(4)), hostId, false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("bloqueo general");
    }

    @Test
    @DisplayName("create con roomTypeId debe guardar bloqueo específico de habitación")
    void create_withRoomType_savesRoomSpecificBlock() {
        UUID hostId = UUID.randomUUID();
        Accommodation accommodation = accommodation(hostId);
        UUID roomTypeId = UUID.randomUUID();
        RoomType roomType = RoomType.builder()
                .id(roomTypeId)
                .name("Doble Serrano")
                .totalUnits(4)
                .accommodation(accommodation)
                .build();

        CreateDateBlockDto dto = CreateDateBlockDto.builder()
                .startDate(TODAY.plusDays(5))
                .endDate(TODAY.plusDays(10))
                .roomTypeId(roomTypeId)
                .unitsBlocked(1)
                .notes("Mantenimiento baño")
                .build();

        when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.findById(roomTypeId)).thenReturn(Optional.of(roomType));
        when(bookingItemRepository.sumBookedUnits(any(), any(), any())).thenReturn(1L);
        when(dateBlockRepository.sumBlockedUnits(any(), any(), any())).thenReturn(0L);
        when(dateBlockRepository.save(any(DateBlock.class))).thenAnswer(invocation -> {
            DateBlock block = invocation.getArgument(0);
            block.setId(UUID.randomUUID());
            block.setCreatedAt(java.time.LocalDateTime.now());
            return block;
        });

        DateBlockResponseDto response = dateBlockService.create(accommodation.getId(), dto, hostId, false);

        assertThat(response.getRoomTypeId()).isEqualTo(roomTypeId);
        assertThat(response.getRoomTypeName()).isEqualTo("Doble Serrano");
        assertThat(response.getUnitsBlocked()).isEqualTo(1);
        verify(dateBlockRepository).save(any(DateBlock.class));
    }

    @Test
    @DisplayName("create con roomTypeId debe rechazar si supera el cupo total")
    void create_withRoomType_whenExceedsTotalUnits_throwsConflict() {
        UUID hostId = UUID.randomUUID();
        Accommodation accommodation = accommodation(hostId);
        UUID roomTypeId = UUID.randomUUID();
        RoomType roomType = RoomType.builder()
                .id(roomTypeId)
                .name("Doble Serrano")
                .totalUnits(2)
                .accommodation(accommodation)
                .build();

        CreateDateBlockDto dto = CreateDateBlockDto.builder()
                .startDate(TODAY.plusDays(5))
                .endDate(TODAY.plusDays(10))
                .roomTypeId(roomTypeId)
                .unitsBlocked(2)
                .build();

        when(accommodationRepository.findById(accommodation.getId())).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.findById(roomTypeId)).thenReturn(Optional.of(roomType));
        when(bookingItemRepository.sumBookedUnits(any(), any(), any())).thenReturn(1L);
        when(dateBlockRepository.sumBlockedUnits(any(), any(), any())).thenReturn(0L);

        assertThatThrownBy(() -> dateBlockService.create(accommodation.getId(), dto, hostId, false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("supera el cupo total");

        verify(dateBlockRepository, never()).save(any(DateBlock.class));
    }

    @Test
    @DisplayName("create debe rechazar fechas inválidas")
    void create_invalidDates_throwsBadRequest() {
        UUID accommodationId = UUID.randomUUID();
        UUID hostId = UUID.randomUUID();

        assertThatThrownBy(() -> dateBlockService.create(
                accommodationId, dto(TODAY.plusDays(5), TODAY.plusDays(5)), hostId, false))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("delete debe rechazar a un usuario que no es dueño del alojamiento")
    void delete_nonOwner_throwsForbidden() {
        UUID hostId = UUID.randomUUID();
        DateBlock block = DateBlock.builder()
                .id(UUID.randomUUID())
                .accommodation(accommodation(hostId))
                .startDate(TODAY.plusDays(1))
                .endDate(TODAY.plusDays(3))
                .build();

        when(dateBlockRepository.findById(block.getId())).thenReturn(Optional.of(block));

        assertThatThrownBy(() -> dateBlockService.delete(block.getId(), UUID.randomUUID(), false))
                .isInstanceOf(ForbiddenException.class);

        verify(dateBlockRepository, never()).delete(any(DateBlock.class));
    }

    private Accommodation accommodation(UUID hostId) {
        User host = User.builder().id(hostId).role(com.upc.demo.entidad.enums.Role.HOST).build();
        return Accommodation.builder()
                .id(UUID.randomUUID())
                .name("Cabaña Serrana")
                .host(host)
                .build();
    }

    private CreateDateBlockDto dto(LocalDate start, LocalDate end) {
        return CreateDateBlockDto.builder()
                .startDate(start)
                .endDate(end)
                .notes("Mantenimiento del deck")
                .build();
    }
}
