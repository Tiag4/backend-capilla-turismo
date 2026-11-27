package com.upc.demo.dto.auth;

import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private UUID id;
    private String email;
    private String name;
    private String lastName;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;

    public static UserProfileDto fromEntity(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}