package com.upc.demo.servicio;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.booking.BookingResponseDto;
import com.upc.demo.dto.booking.CreateBookingDto;
import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponseDto createBooking(CreateBookingDto dto, UserPrincipal currentUser) {
        // 1. Validación de fechas
        if (!dto.getCheckOut().isAfter(dto.getCheckIn())) {
            throw new BadRequestException("La fecha de check-out debe ser posterior a la de check-in");
        }

        long totalNights = ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());
        if (totalNights < 1) {
            throw new BadRequestException("Estadía mínima: 1 noche");
        }

        // 2. Validar alojamiento
        Accommodation accommodation = accommodationRepository.findById(dto.getAccommodationId())
                .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado con id: " + dto.getAccommodationId()));

        if (Boolean.FALSE.equals(accommodation.getIsActive())) {
            throw new BadRequestException("El alojamiento no se encuentra disponible para reservas");
        }

        // 3. Validar capacidad
        if (dto.getGuestCount() > accommodation.getMaxGuests()) {
            throw new BadRequestException("La cantidad de huéspedes supera la capacidad máxima permitida (" + accommodation.getMaxGuests() + ")");
        }

        // 4. Fórmula Anti-Solapamiento
        boolean solapada = bookingRepository.existsOverlappingBooking(
                dto.getAccommodationId(),
                dto.getCheckIn(),
                dto.getCheckOut()
        );
        if (solapada) {
            throw new ConflictException("El alojamiento ya posee una reserva en esas fechas");
        }

        // 5. Generación de Código Idempotente: CAP-<AÑO>-<4_DIGITOS_ALEATORIOS>
        String bookingCode = generateUniqueBookingCode();

        // 6. Cálculo de importe
        BigDecimal totalAmount = accommodation.getPricePerNight().multiply(BigDecimal.valueOf(totalNights));

        // 7. Asociar turista si está autenticado
        User tourist = null;
        if (currentUser != null && currentUser.getId() != null) {
            tourist = userRepository.findById(currentUser.getId()).orElse(null);
        }

        // 8. Crear y persistir la reserva
        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .accommodation(accommodation)
                .tourist(tourist)
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .totalNights((int) totalNights)
                .guestCount(dto.getGuestCount())
                .pricePerNight(accommodation.getPricePerNight())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .guestName(dto.getGuestName().trim())
                .guestEmail(dto.getGuestEmail().trim().toLowerCase())
                .guestPhone(dto.getGuestPhone().trim())
                .guestOrigin(dto.getGuestOrigin() != null ? dto.getGuestOrigin().trim() : null)
                .notes(dto.getNotes() != null ? dto.getNotes().trim() : null)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponseDto(savedBooking);
    }

    private String generateUniqueBookingCode() {
        int year = LocalDate.now().getYear();
        String code;
        do {
            int randomDigits = ThreadLocalRandom.current().nextInt(1000, 10000);
            code = String.format("CAP-%d-%04d", year, randomDigits);
        } while (bookingRepository.findByBookingCode(code).isPresent());
        return code;
    }

    public BookingResponseDto mapToResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .accommodationId(booking.getAccommodation().getId())
                .accommodationName(booking.getAccommodation().getName())
                .touristId(booking.getTourist() != null ? booking.getTourist().getId() : null)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .totalNights(booking.getTotalNights())
                .guestCount(booking.getGuestCount())
                .pricePerNight(booking.getPricePerNight())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .guestOrigin(booking.getGuestOrigin())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
