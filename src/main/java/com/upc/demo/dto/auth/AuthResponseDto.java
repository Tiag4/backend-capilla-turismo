package com.upc.demo.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UserProfileDto user;
}