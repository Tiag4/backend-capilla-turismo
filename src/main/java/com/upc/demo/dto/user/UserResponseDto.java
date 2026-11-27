package com.upc.demo.dto.user;

import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID id;
    private String email;
    private String name;
    private String lastName;
    private String phone;
    private Role role;
    private Boolean isActive;
    private Integer accommodationsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive() != null ? user.getIsActive() : true)
                .accommodationsCount(user.getAccommodations() != null ? user.getAccommodations().size() : 0)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}