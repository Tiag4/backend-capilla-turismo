package com.upc.demo.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los metadatos del medio o imagen cargada en el servidor o CDN")
public class UploadedMediaDto {

    @Schema(description = "Identificador público del archivo (utilizado para borrado o referencias)", example = "capilla-turismo/cabins/cabin-1_abc123")
    private String publicId;

    @Schema(description = "URL accesible públicamente de la imagen", example = "https://res.cloudinary.com/demo/image/upload/v1/capilla-turismo/cabin.jpg")
    private String url;

    @Schema(description = "Formato o extensión de la imagen", example = "webp")
    private String format;

    @Schema(description = "Tamaño del archivo en bytes", example = "1048576")
    private Long sizeBytes;

    @Schema(description = "Fecha y hora en que se procesó la subida", example = "2026-10-05T12:00:00")
    private LocalDateTime createdAt;
}
