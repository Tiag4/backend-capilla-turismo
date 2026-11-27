package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.dto.booking.CreateBookingDto;
import com.upc.demo.dto.booking.BookingResponseDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.*;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceCouponTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DateBlockRepository dateBlockRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private RatePlanRepository ratePlanRepository;

    @Mock
    private BookingItemRepository bookingItemRepository;

    @InjectMocks
    private BookingService bookingService;

    private Accommodation accommodation;
    private UUID accommodationId;

    @BeforeEach
    void setUp() {
        accommodationId = UUID.randomUUID();
        User host = User.builder()
                .id(UUID.randomUUID())
                .name("Host")
                .lastName("Valido")
                .role(Role.HOST)
                .isActive(true)
                .build();

        accommodation = Accommodation.builder()
                .id(accommodationId)
                .name("Cabaña El Paraíso")
                .isActive(true)
                .host(host)
                .pricePerNight(new BigDecimal("50000.00"))
                .maxGuests(4)
                .downPaymentPercentage(50)
                .preparationDays(0)
                .build();
    }

    @Test
    @DisplayName("createBooking: con cupón CAPILLA10 aplica 10% de descuento contable en total y seña")
    void createBooking_withCapilla10Coupon_appliesDiscount() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(2); // 2 nights -> subtotal = 100,000

        CreateBookingDto dto = CreateBookingDto.builder()
                .accommodationId(accommodationId)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guestCount(2)
                .guestName("Carlos López")
                .guestEmail("carlos@gmail.com")
                .guestPhone("+5493548123456")
                .promoCode("CAPILLA10")
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.countByAccommodationId(accommodationId)).thenReturn(0L);
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(dateBlockRepository.existsOverlappingBlock(any(), any(), any())).thenReturn(false);
        when(bookingRepository.findByBookingCode(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BookingResponseDto response = bookingService.createBooking(dto, null);

        assertThat(response).isNotNull();
        assertThat(response.getSubtotalAmount()).isEqualByComparingTo("100000.00");
        assertThat(response.getPromoCode()).isEqualTo("CAPILLA10");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("10000.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("90000.00");
        assertThat(response.getDownPaymentAmount()).isEqualByComparingTo("45000.00");
        assertThat(response.getRemainingBalance()).isEqualByComparingTo("45000.00");
    }

    @Test
    @DisplayName("createBooking: con cupón inválido lanza BadRequestException")
    void createBooking_withInvalidCoupon_throwsBadRequestException() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(2);

        CreateBookingDto dto = CreateBookingDto.builder()
                .accommodationId(accommodationId)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guestCount(2)
                .guestName("Carlos López")
                .guestEmail("carlos@gmail.com")
                .guestPhone("+5493548123456")
                .promoCode("DESCUENTO_FALSO")
                .build();

        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(roomTypeRepository.countByAccommodationId(accommodationId)).thenReturn(0L);
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(dateBlockRepository.existsOverlappingBlock(any(), any(), any())).thenReturn(false);
        when(bookingRepository.findByBookingCode(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(dto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no es válido o ha expirado");
    }
}
