package com.upc.demo.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    private int statusCode;
    private String message;
    private String error;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}