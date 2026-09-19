package com.upc.demo.dto.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvitationDto {

    @NotBlank(message = "El email del prestador invitado es obligatorio")
    @Email(message = "El formato del email no es valido")
    private String email;
}