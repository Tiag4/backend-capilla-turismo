package com.upc.demo.servicio;

import com.upc.demo.config.JwtTokenProvider;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.auth.ChangePasswordDto;
import com.upc.demo.dto.auth.UpdateProfileDto;
import com.upc.demo.dto.auth.UserProfileDto;
import com.upc.demo.dto.common.MessageResponseDto;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.InvitationTokenRepository;
import com.upc.demo.repositorio.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationTokenRepository invitationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("updateProfile actualiza nombre, apellido y teléfono exitosamente")
    void updateProfile_success() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("cabanero@capilladelmonte.gov.ar")
                .name("Roberto")
                .lastName("Gómez")
                .phone("+54 3548 111111")
                .role(Role.HOST)
                .createdAt(LocalDateTime.now())
                .build();

        UpdateProfileDto dto = UpdateProfileDto.builder()
                .name("Roberto Carlos")
                .lastName("Gómez Serrano")
                .phone("+54 3548 999999")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileDto result = authService.updateProfile(userId, dto);

        assertNotNull(result);
        assertEquals("Roberto Carlos", result.getName());
        assertEquals("Gómez Serrano", result.getLastName());
        assertEquals("+54 3548 999999", result.getPhone());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("updateProfile lanza ResourceNotFoundException si el usuario no existe")
    void updateProfile_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        UpdateProfileDto dto = UpdateProfileDto.builder()
                .name("Test")
                .lastName("User")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.updateProfile(userId, dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword actualiza la contraseña exitosamente si la actual coincide")
    void changePassword_success() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("host@capilla.com")
                .password("encoded_old_password")
                .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .currentPassword("OldSecret123!")
                .newPassword("NewSecret456!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("OldSecret123!", "encoded_old_password")).thenReturn(true);
        when(passwordEncoder.matches("NewSecret456!", "encoded_old_password")).thenReturn(false);
        when(passwordEncoder.encode("NewSecret456!")).thenReturn("encoded_new_password");

        MessageResponseDto response = authService.changePassword(userId, dto);

        assertNotNull(response);
        assertEquals("Contraseña actualizada exitosamente", response.getMessage());
        assertEquals("encoded_new_password", existingUser.getPassword());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("changePassword lanza BadRequestException si la contraseña actual no coincide")
    void changePassword_wrongCurrentPassword_throwsException() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .password("encoded_real_password")
                .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .currentPassword("WrongPassword123")
                .newPassword("NewSecret456!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("WrongPassword123", "encoded_real_password")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.changePassword(userId, dto));
        assertEquals("La contraseña actual es incorrecta", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword lanza BadRequestException si la nueva contraseña es igual a la actual")
    void changePassword_samePassword_throwsException() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .password("encoded_password")
                .build();

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .currentPassword("SamePassword123")
                .newPassword("SamePassword123")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("SamePassword123", "encoded_password")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.changePassword(userId, dto));
        assertEquals("La nueva contraseña no puede ser idéntica a la actual", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
