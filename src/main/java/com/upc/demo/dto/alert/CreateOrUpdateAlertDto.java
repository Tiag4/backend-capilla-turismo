package com.upc.demo.dto.alert;

import com.upc.demo.entidad.enums.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateAlertDto {

    @NotBlank(message = "El título de la alerta es obligatorio")
    @Size(max = 180, message = "El título no puede exceder 180 caracteres")
    private String title;

    @NotBlank(message = "El mensaje descriptivo es obligatorio")
    private String message;

    @NotNull(message = "La severidad es obligatoria")
    private AlertSeverity severity;

    @Size(max = 500, message = "El enlace no puede exceder 500 caracteres")
    private String linkUrl;

    @Size(max = 100, message = "El texto del enlace no puede exceder 100 caracteres")
    private String linkText;

    private Boolean isActive;

    private LocalDateTime expiresAt;
}
