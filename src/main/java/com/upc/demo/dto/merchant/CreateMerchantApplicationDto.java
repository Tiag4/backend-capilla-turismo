package com.upc.demo.dto.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMerchantApplicationDto {

    @NotBlank(message = "El nombre o razón social del comercio es obligatorio")
    @Size(max = 180, message = "El nombre no puede superar los 180 caracteres")
    private String businessName;

    @NotBlank(message = "El rubro o categoría comercial es obligatorio")
    @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
    private String category;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Size(max = 180, message = "El titular no puede superar los 180 caracteres")
    private String ownerName;

    @NotBlank(message = "El correo electrónico de contacto es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 180, message = "El correo no puede superar los 180 caracteres")
    private String email;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    private String phone;

    @NotBlank(message = "El número de habilitación municipal es obligatorio")
    @Size(max = 100, message = "El número de habilitación no puede superar los 100 caracteres")
    private String licenseNumber;
}
