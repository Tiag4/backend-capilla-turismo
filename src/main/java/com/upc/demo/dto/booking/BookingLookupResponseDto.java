package com.upc.demo.dto.booking;

import com.upc.demo.dto.accommodation.AccommodationSummaryDto;
import com.upc.demo.entidad.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingLookupResponseDto {

    private UUID id;
    private String bookingCode;
    private UUID accommodationId;
    private String accommodationName;
    private AccommodationSummaryDto accommodation;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalNights;
    private Integer guestCount;
    private BigDecimal pricePerNight;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String guestName;
    private LocalDateTime createdAt;
}
