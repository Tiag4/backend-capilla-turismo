package com.upc.demo.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectReviewDto {

    @NotBlank(message = "La observación de moderación es obligatoria")
    @Size(max = 1000, message = "La observación no puede superar los 1000 caracteres")
    private String notes;
}
