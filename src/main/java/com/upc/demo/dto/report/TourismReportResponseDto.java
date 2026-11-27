package com.upc.demo.dto.report;

import com.upc.demo.dto.booking.BookingResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourismReportResponseDto {

    private String period;
    private String periodLabel;
    private String dateRangeLabel;
    private Double averageOccupancy;
    private BigDecimal totalRevenue;
    private Integer totalBookings;
    private Integer totalGuests;
    private Double averageStayNights;

    @Builder.Default
    private List<OccupancyDataPointDto> chartData = new ArrayList<>();

    @Builder.Default
    private List<OriginStatDto> origins = new ArrayList<>();

    @Builder.Default
    private List<BookingResponseDto> bookings = new ArrayList<>();
}
