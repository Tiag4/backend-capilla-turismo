package com.upc.demo.dto.calendar;

import com.upc.demo.entidad.enums.DateBlockReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateBlockResponseDto {

    private UUID id;
    private UUID accommodationId;
    private String accommodationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private DateBlockReason reason;
    private String notes;
    private LocalDateTime createdAt;
}
