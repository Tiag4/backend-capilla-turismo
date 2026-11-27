package com.upc.demo.dto.review;

import com.upc.demo.entidad.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEligibilityDto {

    private String bookingCode;
    private UUID accommodationId;
    private String accommodationName;
    private String guestName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BookingStatus bookingStatus;
    private Boolean alreadyReviewed;
    private Boolean canReview;
    private String reason;
}
