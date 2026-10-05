package com.upc.demo.servicio.storage.impl;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.servicio.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

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

            optimizeAndSave(file, destinationFile, extension);
            long finalSizeBytes = Files.exists(destinationFile) ? Files.size(destinationFile) : file.getSize();

            String publicId = safeFolder + "/" + uniqueFileName;
            String relativeUrl = "/uploads/" + safeFolder + "/" + uniqueFileName;
            String publicUrl = this.baseUrl.isEmpty() ? relativeUrl : this.baseUrl + relativeUrl;

            log.info("Archivo local guardado y optimizado con éxito. PublicId: {}, Tamaño final: {} bytes, Path: {}",
                    publicId, finalSizeBytes, destinationFile);

            return UploadedMediaDto.builder()
                    .publicId(publicId)
                    .url(publicUrl)
                    .format(extension.toLowerCase())
                    .sizeBytes(finalSizeBytes)
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

    private void optimizeAndSave(MultipartFile file, Path destinationFile, String extension) throws IOException {
        String ext = extension.toLowerCase();
        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return;
        }

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int maxWidth = 1920;
            int maxHeight = 1080;

            boolean needsResize = originalWidth > maxWidth || originalHeight > maxHeight;
            int targetWidth = originalWidth;
            int targetHeight = originalHeight;

            if (needsResize) {
                double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
                targetWidth = Math.max(1, (int) Math.round(originalWidth * ratio));
                targetHeight = Math.max(1, (int) Math.round(originalHeight * ratio));
            }

            int imageType = (ext.equals("png") && originalImage.getColorModel().hasAlpha())
                    ? BufferedImage.TYPE_INT_ARGB
                    : BufferedImage.TYPE_INT_RGB;

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, imageType);
            Graphics2D g2d = resizedImage.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            } finally {
                g2d.dispose();
            }

            if (ext.equals("jpg") || ext.equals("jpeg")) {
                saveOptimizedJpeg(resizedImage, destinationFile, 0.85f);
            } else {
                ImageIO.write(resizedImage, "png", destinationFile.toFile());
            }
        } catch (Exception e) {
            log.warn("No se pudo optimizar la imagen con ImageIO, guardando archivo original: {}", e.getMessage());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void saveOptimizedJpeg(BufferedImage image, Path destinationFile, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", destinationFile.toFile());
            return;
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(destinationFile.toFile())) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
