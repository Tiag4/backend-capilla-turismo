package com.upc.demo.dto.moderation;

import com.upc.demo.entidad.enums.ImageModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationActionDto {
    private ImageModerationStatus status;
    private String notes;
}
