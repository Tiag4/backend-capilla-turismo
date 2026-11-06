package com.upc.demo.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationReviewsDto {

    private List<ReviewResponseDto> reviews;
    private Long totalReviews;
    private Double averageRating;
    private Double cleanlinessAverage;
    private Double hostAverage;
    private Double locationAverage;
}
