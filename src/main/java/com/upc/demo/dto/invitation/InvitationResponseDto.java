package com.upc.demo.dto.invitation;

import com.upc.demo.entidad.InvitationToken;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponseDto {

    private UUID id;
    private String token;
    private String email;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private UUID createdById;
    private LocalDateTime createdAt;

    public static InvitationResponseDto fromEntity(InvitationToken entity) {
        return InvitationResponseDto.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .email(entity.getEmail())
                .expiresAt(entity.getExpiresAt())
                .usedAt(entity.getUsedAt())
                .createdById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}