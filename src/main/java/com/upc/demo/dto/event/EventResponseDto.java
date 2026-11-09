package com.upc.demo.dto.event;

import com.upc.demo.entidad.enums.EventCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDto {

    private UUID id;
    private String title;
    private String description;
    private EventCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private String timeString;
    private String location;
    private boolean isFree;
    private BigDecimal price;
    private String imageUrl;
    private boolean isActive;
    private boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
