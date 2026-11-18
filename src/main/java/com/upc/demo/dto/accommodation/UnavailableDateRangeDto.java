package com.upc.demo.dto.accommodation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rango de fechas no disponible para reserva (bloqueo manual, reserva activa o colchón de preparación)")
public class UnavailableDateRangeDto {

    @Schema(description = "Identificador único sintético del rango bloqueado", example = "booking-6944bf94-1a3b-48aa-b541-11a51a94e80e")
    private String id;

    @Schema(description = "ID del alojamiento", example = "6944bf94-1a3b-48aa-b541-11a51a94e80e")
    private UUID accommodationId;

    @Schema(description = "ID del tipo de habitación afectado (nulo si aplica a todo el establecimiento)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID roomTypeId;

    @Schema(description = "Fecha de inicio del bloqueo (primer día no disponible)", example = "2026-12-01")
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del bloqueo (día en que finaliza o se restablece el check-in)", example = "2026-12-05")
    private LocalDate endDate;

    @Schema(description = "Motivo de la indisponibilidad (BOOKED, MAINTENANCE, PERSONAL_USE, SEASON_CLOSED, PREPARATION)", example = "BOOKED")
    private String reason;

    @Schema(description = "Tipo de origen del bloqueo (BOOKING, DATE_BLOCK, PREPARATION)", example = "BOOKING")
    private String type;

    @Schema(description = "Notas descriptivas no confidenciales", example = "Mantenimiento general programado")
    private String notes;
}
