package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.servicio.storage.impl.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageService(tempDir.toString(), "http://localhost:8080");
    }

    @Test
    @DisplayName("Debe subir exitosamente un archivo válido y retornar UploadedMediaDto")
    void upload_validFile_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        UploadedMediaDto result = storageService.upload(file, "cabins");

        assertNotNull(result);
        assertNotNull(result.getPublicId());
        assertTrue(result.getPublicId().startsWith("capilla-turismo/cabins/"));
        assertTrue(result.getUrl().startsWith("http://localhost:8080/uploads/capilla-turismo/cabins/"));
        assertEquals("jpg", result.getFormat());
        assertTrue(result.getSizeBytes() > 0);
        assertNotNull(result.getCreatedAt());

        Path storedFile = tempDir.resolve(result.getPublicId());
        assertTrue(Files.exists(storedFile));
    }

    @Test
    @DisplayName("Debe rechazar la subida si el archivo está vacío")
    void upload_emptyFile_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThrows(BadRequestException.class, () -> storageService.upload(file, "cabins"));
    }

    @Test
    @DisplayName("Debe eliminar un archivo existente por su publicId")
    void delete_existingFile_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to-delete.png",
                "image/png",
                "sample data".getBytes()
        );

        UploadedMediaDto uploaded = storageService.upload(file, "attractions");
        Path storedFile = tempDir.resolve(uploaded.getPublicId());
        assertTrue(Files.exists(storedFile));

        storageService.delete(uploaded.getPublicId());
        assertFalse(Files.exists(storedFile));
    }

    @Test
    @DisplayName("Debe ignorar silenciosamente delete con publicId nulo o en blanco")
    void delete_blankPublicId_noop() {
        assertDoesNotThrow(() -> storageService.delete(null));
        assertDoesNotThrow(() -> storageService.delete("   "));
    }
}
