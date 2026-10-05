package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.servicio.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media y Multimedia", description = "Servicio agnóstico para carga, optimización y borrado de imágenes")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    @Operation(
            summary = "Cargar imagen multimedia",
            description = "Sube un archivo de imagen en formato JPEG, PNG o WEBP con un peso máximo de 5MB. Retorna la URL pública y el publicId asignado. Los prestadores (HOST) solo pueden subir a 'accommodations'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagen subida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo no enviado, vacío, formato incompatible o excede los 5MB"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "No cuenta con el rol HOST o ADMIN o intentó subir a una carpeta no autorizada")
    })
    public ResponseEntity<UploadedMediaDto> upload(
            @Parameter(description = "Archivo binario de la imagen (máx 5MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Carpeta lógica de destino (ej: accommodations, attractions)", example = "accommodations")
            @RequestParam(value = "folder", required = false, defaultValue = "accommodations") String folder,
            @AuthenticationPrincipal UserPrincipal user) {

        validateFile(file);

        String targetFolder = resolveFolderForUser(folder, user);

        UploadedMediaDto result = storageService.upload(file, targetFolder);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{*publicId}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar imagen por identificador público (Path)",
            description = "Elimina permanentemente una imagen del almacenamiento configurado utilizando su publicId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagen eliminada satisfactoriamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador público del archivo a eliminar", required = true, example = "capilla-turismo/xyz123")
            @PathVariable("publicId") String publicId) {

        String cleanedPublicId = cleanPublicId(publicId);
        storageService.delete(cleanedPublicId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar imagen por identificador público (Query param)",
            description = "Alternativa para eliminar una imagen enviando el publicId como query parameter."
    )
    public ResponseEntity<Void> deleteByQueryParam(
            @Parameter(description = "Identificador público del archivo a eliminar", required = true)
            @RequestParam("publicId") String publicId) {

        String cleanedPublicId = cleanPublicId(publicId);
        storageService.delete(cleanedPublicId);
        return ResponseEntity.noContent().build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debe proporcionar un archivo de imagen no vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido de 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Formato no soportado (" + contentType + "). Formatos admitidos: JPEG, PNG, WEBP");
        }
    }

    private String cleanPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException("Debe indicar un publicId válido");
        }
        if (publicId.startsWith("/")) {
            return publicId.substring(1);
        }
        return publicId;
    }

    private String resolveFolderForUser(String folder, UserPrincipal user) {
        String requestedFolder = (folder != null && !folder.isBlank()) ? folder.trim().toLowerCase() : "accommodations";

        if (user != null && user.getRole() == Role.HOST) {
            if (!requestedFolder.equals("accommodations") && !requestedFolder.equals("capilla-turismo/accommodations")) {
                throw new ForbiddenException("Los prestadores (HOST) solo tienen permitido subir imágenes a la carpeta de alojamientos ('accommodations')");
            }
            return "accommodations";
        }

        return requestedFolder;
    }
}
