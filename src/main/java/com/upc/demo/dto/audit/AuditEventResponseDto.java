package com.upc.demo.dto.audit;

import com.upc.demo.entidad.AuditEvent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventResponseDto {

    private UUID id;
    private String actionType;
    private String description;
    private String operatorName;
    private String operatorEmail;
    private String targetEntity;
    private String targetId;
    private String details;
    private LocalDateTime timestamp;

    public static AuditEventResponseDto fromEntity(AuditEvent entity) {
        if (entity == null) {
            return null;
        }
        return AuditEventResponseDto.builder()
                .id(entity.getId())
                .actionType(entity.getActionType())
                .description(entity.getDescription())
                .operatorName(entity.getOperatorName())
                .operatorEmail(entity.getOperatorEmail())
                .targetEntity(entity.getTargetEntity())
                .targetId(entity.getTargetId())
                .details(entity.getDetails())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
