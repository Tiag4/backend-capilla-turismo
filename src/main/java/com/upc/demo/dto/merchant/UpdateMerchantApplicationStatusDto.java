package com.upc.demo.dto.merchant;

import com.upc.demo.entidad.enums.MerchantApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMerchantApplicationStatusDto {

    @NotNull(message = "El nuevo estado de la solicitud es obligatorio")
    private MerchantApplicationStatus status;

    private String notes;
}
