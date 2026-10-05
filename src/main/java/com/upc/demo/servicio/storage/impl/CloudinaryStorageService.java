package com.upc.demo.servicio.storage.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.servicio.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Adaptador de almacenamiento en la nube Cloudinary con optimizaciones de entrega y CDN.
 */
@Slf4j
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public UploadedMediaDto upload(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BadRequestException("No se puede subir un archivo vacío");
        }

        try {
            String baseFolder = "capilla-turismo";
            String targetFolder = (folder != null && !folder.isBlank()) ? folder.trim() : "general";
            String safeFolder = targetFolder.startsWith(baseFolder) ? targetFolder : baseFolder + "/" + targetFolder;

            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", safeFolder,
                    "resource_type", "image",
                    "overwrite", true,
                    "transformation", new com.cloudinary.Transformation<>()
                            .width(1920)
                            .height(1080)
                            .crop("limit")
                            .quality("auto")
                            .fetchFormat("auto")
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");
            Number bytes = (Number) uploadResult.get("bytes");

            log.info("Archivo subido a Cloudinary exitosamente. PublicId: {}, URL: {}", publicId, secureUrl);

            return UploadedMediaDto.builder()
                    .publicId(publicId)
                    .url(secureUrl)
                    .format(format != null ? format.toLowerCase() : "")
                    .sizeBytes(bytes != null ? bytes.longValue() : file.getSize())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            log.error("Fallo al subir archivo a Cloudinary", e);
            throw new BadRequestException("Error al procesar la subida con Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Resultado de eliminación en Cloudinary para {}: {}", publicId, result);
        } catch (IOException e) {
            log.error("Error al eliminar archivo de Cloudinary: {}", publicId, e);
            throw new BadRequestException("Error al eliminar imagen de Cloudinary: " + e.getMessage());
        }
    }
}
