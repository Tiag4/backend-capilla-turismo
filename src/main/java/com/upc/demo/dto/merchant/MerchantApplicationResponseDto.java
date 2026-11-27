package com.upc.demo.dto.merchant;

import com.upc.demo.entidad.enums.MerchantApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantApplicationResponseDto {

    private UUID id;
    private String businessName;
    private String category;
    private String ownerName;
    private String email;
    private String phone;
    private String licenseNumber;
    private MerchantApplicationStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
