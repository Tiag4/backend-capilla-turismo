package com.upc.demo.controlador;

import com.upc.demo.config.GlobalExceptionHandler;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.servicio.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MediaController mediaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mediaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Sube imagen válida y retorna 201 Created")
    void upload_validImage_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cabin-main.webp",
                "image/webp",
                "fake image bytes".getBytes()
        );

        UploadedMediaDto responseDto = UploadedMediaDto.builder()
                .publicId("cabins/cabin-main-uuid")
                .url("https://res.cloudinary.com/demo/image/upload/v1/cabins/cabin-main-uuid.webp")
                .format("webp")
                .sizeBytes((long) file.getBytes().length)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageService.upload(any(), eq("cabins"))).thenReturn(responseDto);

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("folder", "cabins")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value("cabins/cabin-main-uuid"))
                .andExpect(jsonPath("$.format").value("webp"))
                .andExpect(jsonPath("$.url").value(responseDto.getUrl()));

        verify(storageService, times(1)).upload(any(), eq("cabins"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Rechaza formato no permitido (ej. PDF) con 400 Bad Request")
    void upload_invalidMimeType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Formato no soportado")));

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Rechaza archivo vacío con 400 Bad Request")
    void upload_emptyFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Rechaza archivo mayor a 5MB con 400 Bad Request")
    void upload_oversizedFile_returns400() throws Exception {
        byte[] oversizedBytes = new byte[5 * 1024 * 1024 + 1]; // 5MB + 1 byte
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "huge.jpg",
                "image/jpeg",
                oversizedBytes
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El archivo excede el tamaño máximo permitido de 5MB"));

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("DELETE /api/v1/media/{publicId} - Elimina recurso y retorna 204 No Content")
    void delete_byPathVariable_returns204() throws Exception {
        doNothing().when(storageService).delete("cabins/img-123");

        mockMvc.perform(delete("/api/v1/media/cabins/img-123"))
                .andExpect(status().isNoContent());

        verify(storageService, times(1)).delete("cabins/img-123");
    }

    @Test
    @DisplayName("DELETE /api/v1/media?publicId=... - Elimina recurso por query param y retorna 204")
    void delete_byQueryParam_returns204() throws Exception {
        doNothing().when(storageService).delete("attractions/uritorco_1");

        mockMvc.perform(delete("/api/v1/media")
                        .param("publicId", "attractions/uritorco_1"))
                .andExpect(status().isNoContent());

        verify(storageService, times(1)).delete("attractions/uritorco_1");
    }
}
