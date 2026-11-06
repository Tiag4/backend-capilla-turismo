package com.upc.demo.servicio;

import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.dto.booking.CreateBookingDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingEmailServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("createBooking debe despachar el voucher por email al registrar la reserva")
    void createBooking_sendsVoucherEmail() {
        UUID accommodationId = UUID.randomUUID();
        User host = User.builder().id(UUID.randomUUID()).email("host@capilla.com").build();
        Accommodation accommodation = Accommodation.builder()
                .id(accommodationId)
                .host(host)
                .name("Cabaña Serrana")
                .pricePerNight(BigDecimal.valueOf(80000))
                .maxGuests(4)
                .isActive(true)
                .downPaymentPercentage(50)
                .build();

        CreateBookingDto dto = CreateBookingDto.builder()
                .accommodationId(accommodationId)
                .checkIn(LocalDate.now().plusDays(2))
                .checkOut(LocalDate.now().plusDays(5))
                .guestCount(2)
                .guestName("Martina Gómez")
                .guestEmail("turista@email.com")
                .guestPhone("+54 9 351 555-1234")
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(bookingRepository.findByBookingCode(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(UUID.randomUUID());
            booking.setCreatedAt(java.time.LocalDateTime.now());
            return booking;
        });
        when(emailService.sendVoucherEmails(any(Booking.class))).thenReturn(true);

        bookingService.createBooking(dto, null);

        verify(emailService).sendVoucherEmails(any(Booking.class));
    }

    @Test
    @DisplayName("resendVoucherToGuest debe enviar el voucher al turista por código y email")
    void resendVoucherByCode_sendsEmail() {
        Booking booking = buildBooking();
        when(bookingRepository.findByBookingCodeAndGuestEmailIgnoreCase("CAP-2026-4567", "turista@email.com"))
                .thenReturn(Optional.of(booking));
        when(emailService.sendVoucherToGuest(booking)).thenReturn(true);

        boolean sent = bookingService.resendVoucherToGuest("CAP-2026-4567", "turista@email.com");

        assertThat(sent).isTrue();
        verify(emailService).sendVoucherToGuest(booking);
    }

    @Test
    @DisplayName("resendVoucherToGuest por id debe rechazar a un usuario que no es dueño")
    void resendVoucherById_deniesNonOwner() {
        Booking booking = buildBooking();
        UUID ownerId = booking.getAccommodation().getHost().getId();
        UUID anotherUserId = UUID.randomUUID();
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.resendVoucherToGuest(booking.getId(), anotherUserId, false))
                .isInstanceOf(ForbiddenException.class);
    }

    private Booking buildBooking() {
        User host = User.builder()
                .id(UUID.randomUUID())
                .name("Carla")
                .lastName("Pérez")
                .email("host@capilla.com")
                .build();
        Accommodation accommodation = Accommodation.builder()
                .id(UUID.randomUUID())
                .name("Cabaña Serrana")
                .host(host)
                .build();
        return Booking.builder()
                .id(UUID.randomUUID())
                .bookingCode("CAP-2026-4567")
                .accommodation(accommodation)
                .guestName("Martina Gómez")
                .guestEmail("turista@email.com")
                .guestPhone("+54 9 351 555-1234")
                .totalAmount(BigDecimal.valueOf(100000))
                .downPaymentPercentage(50)
                .downPaymentAmount(BigDecimal.valueOf(50000))
                .remainingBalance(BigDecimal.valueOf(50000))
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(3))
                .totalNights(2)
                .guestCount(2)
                .pricePerNight(BigDecimal.valueOf(50000))
                .status(BookingStatus.PENDING)
                .build();
    }
}
