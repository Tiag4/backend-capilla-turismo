package com.upc.demo.dto.booking;

import com.upc.demo.entidad.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingStatusDto {

    @NotNull(message = "El nuevo estado de la reserva es obligatorio")
    private BookingStatus status;
}
