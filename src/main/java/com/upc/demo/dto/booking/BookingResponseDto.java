package com.upc.demo.dto.booking;

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
public class BookingResponseDto {

    private UUID id;
    private String bookingCode;
    private UUID accommodationId;
    private String accommodationName;
    private UUID touristId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalNights;
    private Integer guestCount;
    private BigDecimal pricePerNight;
    private BigDecimal totalAmount;
    private Integer downPaymentPercentage;
    private BigDecimal downPaymentAmount;
    private BigDecimal remainingBalance;
    private String bankAccountHolder;
    private String bankCbu;
    private String bankAlias;
    private String bankName;
    private BookingStatus status;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String guestOrigin;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
