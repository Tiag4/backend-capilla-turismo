package com.upc.demo.controlador;

import com.upc.demo.config.GlobalExceptionHandler;
import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.media.UploadedMediaDto;
import com.upc.demo.entidad.enums.Role;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

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

    private UserPrincipal hostUser;
    private UserPrincipal adminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mediaController)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().equals(UserPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                                  org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                                  org.springframework.web.context.request.NativeWebRequest webRequest,
                                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        java.security.Principal principal = webRequest.getUserPrincipal();
                        if (principal instanceof org.springframework.security.core.Authentication auth) {
                            return auth.getPrincipal();
                        }
                        return null;
                    }
                })
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        hostUser = UserPrincipal.builder()
                .id(UUID.randomUUID())
                .email("host@capilla.com")
                .role(Role.HOST)
                .build();

        adminUser = UserPrincipal.builder()
                .id(UUID.randomUUID())
                .email("admin@capilla.com")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Host sube imagen a accommodations exitosamente (201 Created)")
    void upload_hostToAccommodations_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cabin-main.webp",
                "image/webp",
                "fake image bytes".getBytes()
        );

        UploadedMediaDto responseDto = UploadedMediaDto.builder()
                .publicId("capilla-turismo/accommodations/cabin-uuid")
                .url("https://res.cloudinary.com/demo/image/upload/v1/capilla-turismo/accommodations/cabin-uuid.webp")
                .format("webp")
                .sizeBytes((long) file.getBytes().length)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageService.upload(any(), eq("accommodations"))).thenReturn(responseDto);

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("folder", "accommodations")
                        .principal(new TestingAuthenticationToken(hostUser, null))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value("capilla-turismo/accommodations/cabin-uuid"))
                .andExpect(jsonPath("$.format").value("webp"))
                .andExpect(jsonPath("$.url").value(responseDto.getUrl()));

        verify(storageService, times(1)).upload(any(), eq("accommodations"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Host intentando subir a attractions es rechazado con 403 Forbidden")
    void upload_hostToAttractions_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake-attraction.jpg",
                "image/jpeg",
                "fake image bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("folder", "attractions")
                        .principal(new TestingAuthenticationToken(hostUser, null))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Los prestadores (HOST) solo tienen permitido subir imágenes a la carpeta de alojamientos")));

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/media/upload - Admin puede subir tanto a attractions como a accommodations")
    void upload_adminCanUploadToAnyFolder_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "uritorco.webp",
                "image/webp",
                "fake image bytes".getBytes()
        );

        UploadedMediaDto responseDto = UploadedMediaDto.builder()
                .publicId("capilla-turismo/attractions/uritorco-uuid")
                .url("https://res.cloudinary.com/demo/image/upload/v1/capilla-turismo/attractions/uritorco-uuid.webp")
                .format("webp")
                .sizeBytes((long) file.getBytes().length)
                .createdAt(LocalDateTime.now())
                .build();

        when(storageService.upload(any(), eq("attractions"))).thenReturn(responseDto);

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("folder", "attractions")
                        .principal(new TestingAuthenticationToken(adminUser, null))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value("capilla-turismo/attractions/uritorco-uuid"));

        verify(storageService, times(1)).upload(any(), eq("attractions"));
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
                        .principal(new TestingAuthenticationToken(adminUser, null))
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
                        .principal(new TestingAuthenticationToken(adminUser, null))
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
                        .principal(new TestingAuthenticationToken(adminUser, null))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El archivo excede el tamaño máximo permitido de 5MB"));

        verify(storageService, never()).upload(any(), any());
    }

    @Test
    @DisplayName("DELETE /api/v1/media/{publicId} - Elimina recurso y retorna 204 No Content")
    void delete_byPathVariable_returns204() throws Exception {
        doNothing().when(storageService).delete("capilla-turismo/accommodations/img-123");

        mockMvc.perform(delete("/api/v1/media/capilla-turismo/accommodations/img-123"))
                .andExpect(status().isNoContent());

        verify(storageService, times(1)).delete("capilla-turismo/accommodations/img-123");
    }

    @Test
    @DisplayName("DELETE /api/v1/media?publicId=... - Elimina recurso por query param y retorna 204")
    void delete_byQueryParam_returns204() throws Exception {
        doNothing().when(storageService).delete("capilla-turismo/attractions/uritorco_1");

        mockMvc.perform(delete("/api/v1/media")
                        .param("publicId", "capilla-turismo/attractions/uritorco_1"))
                .andExpect(status().isNoContent());

        verify(storageService, times(1)).delete("capilla-turismo/attractions/uritorco_1");
    }
}
