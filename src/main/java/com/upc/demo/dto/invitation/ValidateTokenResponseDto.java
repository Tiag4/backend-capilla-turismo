package com.upc.demo.dto.invitation;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTokenResponseDto {

    private boolean valid;
    private String email;
    private LocalDateTime expiresAt;
    private String message;
}