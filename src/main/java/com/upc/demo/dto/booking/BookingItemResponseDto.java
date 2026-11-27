package com.upc.demo.dto.booking;

import com.upc.demo.entidad.BookingItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemResponseDto {

    private UUID id;
    private UUID roomTypeId;
    private String roomTypeName;
    private UUID ratePlanId;
    private String ratePlanName;
    private Integer quantity;
    private Integer guests;
    private BigDecimal pricePerNight;
    private BigDecimal subtotal;

    public static BookingItemResponseDto fromEntity(BookingItem entity) {
        if (entity == null) {
            return null;
        }
        return BookingItemResponseDto.builder()
                .id(entity.getId())
                .roomTypeId(entity.getRoomType() != null ? entity.getRoomType().getId() : null)
                .roomTypeName(entity.getRoomType() != null ? entity.getRoomType().getName() : null)
                .ratePlanId(entity.getRatePlan() != null ? entity.getRatePlan().getId() : null)
                .ratePlanName(entity.getRatePlan() != null ? entity.getRatePlan().getName() : null)
                .quantity(entity.getQuantity())
                .guests(entity.getGuests())
                .pricePerNight(entity.getPricePerNight())
                .subtotal(entity.getSubtotal())
                .build();
    }
}
