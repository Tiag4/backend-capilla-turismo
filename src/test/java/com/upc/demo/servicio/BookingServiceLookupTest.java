package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.booking.BookingLookupResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.repositorio.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceLookupTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("lookup: retorna los datos completos y calculados de la reserva cuando el código y email coinciden ignorando mayúsculas y espacios")
    void lookup_successful() {
        UUID bookingId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Accommodation accommodation = Accommodation.builder()
                .id(accommodationId)
                .name("Cabañas Valle del Sol")
                .locality("La Toma")
                .address("Camino a La Toma km 1.5")
                .phone("+54 9 3548 40-1122")
                .downPaymentPercentage(50)
                .bankAccountHolder("Valle del Sol SRL")
                .bankCbu("0110485530048500123456")
                .bankAlias("VALLEDELSOL.CAPILLA")
                .bankName("Banco de la Nación Argentina")
                .images(Collections.emptyList())
                .build();

        Booking booking = Booking.builder()
                .id(bookingId)
                .bookingCode("CAP-2026-8492")
                .guestName("Martino Gómez")
                .guestEmail("turista@capilladelmonte.gov.ar")
                .guestPhone("+54 9 351 555-1234")
                .accommodation(accommodation)
                .checkIn(LocalDate.of(2026, 9, 18))
                .checkOut(LocalDate.of(2026, 9, 22))
                .totalNights(4)
                .guestCount(2)
                .pricePerNight(BigDecimal.valueOf(95000))
                .totalAmount(BigDecimal.valueOf(380000))
                .downPaymentPercentage(50)
                .downPaymentAmount(BigDecimal.valueOf(190000))
                .remainingBalance(BigDecimal.valueOf(190000))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingRepository.findByBookingCodeIgnoreCaseAndGuestEmailIgnoreCase("CAP-2026-8492", "turista@capilladelmonte.gov.ar"))
                .thenReturn(Optional.of(booking));

        BookingLookupResponseDto response = bookingService.lookup(" cap-2026-8492 ", " TURISTA@CAPILLADELMONTE.GOV.AR ");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(bookingId);
        assertThat(response.getBookingCode()).isEqualTo("CAP-2026-8492");
        assertThat(response.getGuestName()).isEqualTo("Martino Gómez");
        assertThat(response.getGuestEmail()).isEqualTo("turista@capilladelmonte.gov.ar");
        assertThat(response.getGuestPhone()).isEqualTo("+54 9 351 555-1234");
        assertThat(response.getHostPhone()).isEqualTo("+54 9 3548 40-1122");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(380000));
        assertThat(response.getDownPaymentAmount()).isEqualByComparingTo(BigDecimal.valueOf(190000));
        assertThat(response.getRemainingBalance()).isEqualByComparingTo(BigDecimal.valueOf(190000));
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getBankAlias()).isEqualTo("VALLEDELSOL.CAPILLA");
        assertThat(response.getAccommodation()).isNotNull();
        assertThat(response.getAccommodation().getPhone()).isEqualTo("+54 9 3548 40-1122");
    }

    @Test
    @DisplayName("lookup: lanza BadRequestException si el código o el email son nulos o están en blanco")
    void lookup_blankCodeOrEmail_throwsBadRequestException() {
        assertThatThrownBy(() -> bookingService.lookup(null, "turista@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("El c");

        assertThatThrownBy(() -> bookingService.lookup("CAP-123", "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("El c");
    }

    @Test
    @DisplayName("lookup: lanza ResourceNotFoundException si la reserva no existe")
    void lookup_notFound_throwsResourceNotFoundException() {
        when(bookingRepository.findByBookingCodeIgnoreCaseAndGuestEmailIgnoreCase("CAP-999", "nadie@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.lookup("CAP-999", "nadie@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No se encontr");
    }
}
