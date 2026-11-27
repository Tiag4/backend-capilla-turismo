package com.upc.demo.dto.alert;

import com.upc.demo.entidad.enums.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDto {
    private UUID id;
    private String title;
    private String message;
    private AlertSeverity severity;
    private String linkUrl;
    private String linkText;
    private boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
