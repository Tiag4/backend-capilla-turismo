package com.upc.demo.servicio;

import com.upc.demo.config.JwtTokenProvider;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.auth.*;
import com.upc.demo.dto.common.MessageResponseDto;
import com.upc.demo.entidad.PasswordResetCode;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.InvitationTokenRepository;
import com.upc.demo.repositorio.PasswordResetCodeRepository;
import com.upc.demo.repositorio.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login rechaza a un prestador suspendido")
    void login_suspendedHost_throwsForbidden() {
        User suspended = User.builder()
                .id(UUID.randomUUID())
                .email("host@capilla.com")
                .password("encoded_password")
                .role(Role.HOST)
                .isActive(false)
                .build();

        when(userRepository.findByEmail("host@capilla.com")).thenReturn(Optional.of(suspended));
        when(passwordEncoder.matches("Secret123!", "encoded_password")).thenReturn(true);

        LoginRequestDto dto = LoginRequestDto.builder()
                .email("host@capilla.com")
                .password("Secret123!")
                .build();

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.login(dto));
        assertTrue(ex.getMessage().contains("suspendida"));
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("changePassword actualiza la contraseña si la actual es correcta")
    void changePassword_validCurrentPassword_updatesPassword() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
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
        verify(userRepository).save(existingUser);
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

    @Test
    @DisplayName("sendPasswordResetOtp genera código y envía correo para usuario existente")
    void sendPasswordResetOtp_existingUser_generatesCodeAndSendsEmail() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Roberto")
                .email("host@capilladelmonte.gov.ar")
                .build();

        when(userRepository.findByEmail("host@capilladelmonte.gov.ar")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findAllByEmailAndUsedAtIsNull("host@capilladelmonte.gov.ar"))
                .thenReturn(Collections.emptyList());

        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto("host@capilladelmonte.gov.ar");
        MessageResponseDto response = authService.sendPasswordResetOtp(dto);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("Si el correo electrónico está registrado"));

        ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);
        verify(passwordResetCodeRepository).save(captor.capture());
        PasswordResetCode savedCode = captor.getValue();
        assertEquals("host@capilladelmonte.gov.ar", savedCode.getEmail());
        assertEquals(6, savedCode.getCode().length());
        assertEquals(0, savedCode.getAttempts());
        assertNotNull(savedCode.getExpiresAt());

        verify(emailService).sendPasswordResetOtp(eq(user.getEmail()), eq(user.getName()), eq(savedCode.getCode()));
    }

    @Test
    @DisplayName("sendPasswordResetOtp retorna mensaje genérico anti-enumeración para usuario inexistente")
    void sendPasswordResetOtp_nonExistingUser_returnsGenericMessage() {
        when(userRepository.findByEmail("inexistente@correo.com")).thenReturn(Optional.empty());

        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto("inexistente@correo.com");
        MessageResponseDto response = authService.sendPasswordResetOtp(dto);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("Si el correo electrónico está registrado"));
        verify(passwordResetCodeRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetOtp(any(), any(), any());
    }

    @Test
    @DisplayName("resetPasswordWithOtp actualiza la clave si el código y datos son válidos")
    void resetPasswordWithOtp_validCode_updatesPassword() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("host@capilladelmonte.gov.ar")
                .password("encoded_old")
                .build();

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .id(UUID.randomUUID())
                .email("host@capilladelmonte.gov.ar")
                .code("123456")
                .attempts(0)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(userRepository.findByEmail("host@capilladelmonte.gov.ar")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc("host@capilladelmonte.gov.ar"))
                .thenReturn(Optional.of(resetCode));
        when(passwordEncoder.encode("NuevaPassword123!")).thenReturn("encoded_new");

        ResetPasswordRequestDto dto = new ResetPasswordRequestDto("host@capilladelmonte.gov.ar", "123456", "NuevaPassword123!");
        MessageResponseDto response = authService.resetPasswordWithOtp(dto);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("exitosamente"));
        assertEquals("encoded_new", user.getPassword());
        assertNotNull(resetCode.getUsedAt());
        verify(userRepository).save(user);
        verify(passwordResetCodeRepository).save(resetCode);
    }

    @Test
    @DisplayName("resetPasswordWithOtp falla si el código expiró")
    void resetPasswordWithOtp_expiredCode_throwsBadRequest() {
        User user = User.builder().email("host@capilladelmonte.gov.ar").build();
        PasswordResetCode expiredCode = PasswordResetCode.builder()
                .email("host@capilladelmonte.gov.ar")
                .code("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(userRepository.findByEmail("host@capilladelmonte.gov.ar")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc("host@capilladelmonte.gov.ar"))
                .thenReturn(Optional.of(expiredCode));

        ResetPasswordRequestDto dto = new ResetPasswordRequestDto("host@capilladelmonte.gov.ar", "123456", "NuevaPassword123!");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.resetPasswordWithOtp(dto));

        assertTrue(ex.getMessage().contains("ha expirado"));
        assertNotNull(expiredCode.getUsedAt());
        verify(passwordResetCodeRepository).save(expiredCode);
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("resetPasswordWithOtp incrementa intentos con código incorrecto")
    void resetPasswordWithOtp_wrongCode_incrementsAttempts() {
        User user = User.builder().email("host@capilladelmonte.gov.ar").build();
        PasswordResetCode resetCode = PasswordResetCode.builder()
                .email("host@capilladelmonte.gov.ar")
                .code("123456")
                .attempts(2)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(userRepository.findByEmail("host@capilladelmonte.gov.ar")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc("host@capilladelmonte.gov.ar"))
                .thenReturn(Optional.of(resetCode));

        ResetPasswordRequestDto dto = new ResetPasswordRequestDto("host@capilladelmonte.gov.ar", "999999", "NuevaPassword123!");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.resetPasswordWithOtp(dto));

        assertTrue(ex.getMessage().contains("Código incorrecto"));
        assertEquals(3, resetCode.getAttempts());
        assertNull(resetCode.getUsedAt());
        verify(passwordResetCodeRepository).save(resetCode);
        verify(userRepository, never()).save(user);
    }
}
