package com.upc.demo.dto.review;

import com.upc.demo.entidad.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {

    private UUID id;
    private UUID accommodationId;
    private String accommodationName;
    private String accommodationImageUrl;
    private String bookingCode;
    private String guestName;
    private String guestOrigin;
    private String stayDate;
    private Integer rating;
    private Integer cleanlinessRating;
    private Integer hostRating;
    private Integer locationRating;
    private String comment;
    private ReviewStatus status;
    private String moderationNotes;
    private LocalDateTime createdAt;
    private LocalDateTime moderatedAt;
}
