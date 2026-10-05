package com.upc.demo.servicio.storage.impl;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.servicio.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adaptador de almacenamiento en disco local (desarrollo, fallback o VPS).
 */
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path rootLocation;
    private final String baseUrl;

    public LocalStorageService(String uploadDir, String baseUrl) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.replaceAll("/+$", "") : "";
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Directorio de almacenamiento local inicializado en: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("No se pudo inicializar el directorio raíz de almacenamiento local: {}", uploadDir, e);
            throw new RuntimeException("Error al inicializar directorio de almacenamiento local", e);
        }
    }

    @Override
    public UploadedMediaDto upload(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BadRequestException("No se puede subir un archivo vacío");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
        String extension = getFileExtension(originalFilename);
        String safeFolder = (folder != null && !folder.isBlank()) ? folder.replaceAll("[^a-zA-Z0-9_-]", "") : "general";

        String uniqueFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path targetFolder = this.rootLocation.resolve(safeFolder).normalize();

        try {
            Files.createDirectories(targetFolder);

            Path destinationFile = targetFolder.resolve(uniqueFileName).normalize();
            if (!destinationFile.getParent().equals(targetFolder)) {
                throw new BadRequestException("No se permite almacenar archivos fuera del directorio de destino");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicId = safeFolder + "/" + uniqueFileName;
            String relativeUrl = "/uploads/" + safeFolder + "/" + uniqueFileName;
            String publicUrl = this.baseUrl.isEmpty() ? relativeUrl : this.baseUrl + relativeUrl;

            log.info("Archivo local guardado con éxito. PublicId: {}, Path: {}", publicId, destinationFile);

            return UploadedMediaDto.builder()
                    .publicId(publicId)
                    .url(publicUrl)
                    .format(extension.toLowerCase())
                    .sizeBytes(file.getSize())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            log.error("Fallo al guardar el archivo localmente: {}", originalFilename, e);
            throw new BadRequestException("Error de I/O al almacenar el archivo en disco: " + e.getMessage());
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            Path fileToDelete = this.rootLocation.resolve(publicId).normalize();
            if (!fileToDelete.startsWith(this.rootLocation)) {
                log.warn("Intento de eliminación de archivo fuera del directorio raíz: {}", publicId);
                throw new BadRequestException("Ruta de archivo no válida");
            }

            boolean deleted = Files.deleteIfExists(fileToDelete);
            if (deleted) {
                log.info("Archivo local eliminado exitosamente: {}", publicId);
            } else {
                log.warn("El archivo local a eliminar no existía en el disco: {}", publicId);
            }
        } catch (IOException e) {
            log.error("Error al eliminar archivo local: {}", publicId, e);
            throw new BadRequestException("Error al eliminar archivo local: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
