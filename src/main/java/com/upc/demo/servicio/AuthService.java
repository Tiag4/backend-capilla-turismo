package com.upc.demo.servicio;

import com.upc.demo.config.JwtTokenProvider;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ForbiddenException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.auth.*;
import com.upc.demo.dto.common.MessageResponseDto;
import com.upc.demo.entidad.InvitationToken;
import com.upc.demo.entidad.PasswordResetCode;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.InvitationTokenRepository;
import com.upc.demo.repositorio.PasswordResetCodeRepository;
import com.upc.demo.repositorio.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ForbiddenException("La cuenta del prestador se encuentra suspendida. Contacta a la Camara de Turismo.");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        return AuthResponseDto.builder()
                .token(token)
                .type("Bearer")
                .user(UserProfileDto.fromEntity(user))
                .build();
    }

    @Transactional
    public AuthResponseDto registerTourist(RegisterTouristDto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ConflictException("El email " + cleanEmail + " ya se encuentra registrado");
        }

        User newUser = User.builder()
                .email(cleanEmail)
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName().trim())
                .lastName(dto.getLastName().trim())
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(Role.TOURIST)
                .build();

        User savedUser = userRepository.save(newUser);
        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return AuthResponseDto.builder()
                .token(token)
                .type("Bearer")
                .user(UserProfileDto.fromEntity(savedUser))
                .build();
    }

    @Transactional
    public AuthResponseDto registerHost(RegisterHostDto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();
        String token = dto.getToken().trim();

        InvitationToken invitation = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token de invitacion no valido o inexistente"));

        if (invitation.getUsedAt() != null) {
            throw new BadRequestException("Este token de invitacion ya ha sido utilizado");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token de invitacion ha expirado");
        }

        if (!invitation.getEmail().equalsIgnoreCase(cleanEmail)) {
            throw new BadRequestException("El email provisto no coincide con el email autorizado en la invitacion");
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ConflictException("El email " + cleanEmail + " ya se encuentra registrado");
        }

        invitation.setUsedAt(LocalDateTime.now());
        invitationTokenRepository.save(invitation);

        User newHost = User.builder()
                .email(cleanEmail)
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName().trim())
                .lastName(dto.getLastName().trim())
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(Role.HOST)
                .build();

        User savedHost = userRepository.save(newHost);
        String jwt = jwtTokenProvider.generateToken(savedHost.getId(), savedHost.getEmail(), savedHost.getRole());

        return AuthResponseDto.builder()
                .token(jwt)
                .type("Bearer")
                .user(UserProfileDto.fromEntity(savedHost))
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        return UserProfileDto.fromEntity(user);
    }

    @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        user.setName(dto.getName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setPhone(dto.getPhone() != null && !dto.getPhone().isBlank() ? dto.getPhone().trim() : null);

        User savedUser = userRepository.save(user);
        return UserProfileDto.fromEntity(savedUser);
    }

    @Transactional
    public MessageResponseDto changePassword(UUID userId, ChangePasswordDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("La nueva contraseña no puede ser idéntica a la actual");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return MessageResponseDto.builder()
                .message("Contraseña actualizada exitosamente")
                .build();
    }

    @Transactional
    public MessageResponseDto sendPasswordResetOtp(ForgotPasswordRequestDto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(cleanEmail);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Invalida codigos activos previos para este email
            List<PasswordResetCode> activeCodes = passwordResetCodeRepository.findAllByEmailAndUsedAtIsNull(cleanEmail);
            for (PasswordResetCode activeCode : activeCodes) {
                activeCode.setUsedAt(LocalDateTime.now());
            }
            passwordResetCodeRepository.saveAll(activeCodes);

            // Genera codigo criptograficamente seguro de 6 digitos (000000 - 999999)
            String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

            PasswordResetCode resetCode = PasswordResetCode.builder()
                    .email(cleanEmail)
                    .code(code)
                    .attempts(0)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            passwordResetCodeRepository.save(resetCode);
            emailService.sendPasswordResetOtp(user.getEmail(), user.getName(), code);
            log.info("Codigo OTP de recuperacion generado para el usuario {}", cleanEmail);
        } else {
            log.info("Solicitud de recuperacion para email no registrado: {} (respuesta anti-enumeracion)", cleanEmail);
        }

        // Respuesta generica obligatoria para prevenir enumeracion de cuentas
        return MessageResponseDto.builder()
                .message("Si el correo electrónico está registrado, recibirás un código de 6 dígitos en breve.")
                .build();
    }

    @Transactional
    public MessageResponseDto resetPasswordWithOtp(ResetPasswordRequestDto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new BadRequestException("El código de recuperación es inválido o ha expirado."));

        PasswordResetCode resetCode = passwordResetCodeRepository
                .findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(cleanEmail)
                .orElseThrow(() -> new BadRequestException("El código de recuperación es inválido o ha expirado."));

        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetCode.setUsedAt(LocalDateTime.now());
            passwordResetCodeRepository.save(resetCode);
            throw new BadRequestException("El código de recuperación ha expirado (validez: 15 minutos). Solicitá uno nuevo.");
        }

        if (resetCode.getAttempts() >= 5) {
            resetCode.setUsedAt(LocalDateTime.now());
            passwordResetCodeRepository.save(resetCode);
            throw new BadRequestException("Has superado el límite de 5 intentos fallidos permitidos. Solicitá un nuevo código.");
        }

        resetCode.setAttempts(resetCode.getAttempts() + 1);

        if (!resetCode.getCode().equals(dto.getCode().trim())) {
            passwordResetCodeRepository.save(resetCode);
            int remaining = 5 - resetCode.getAttempts();
            if (remaining > 0) {
                throw new BadRequestException("Código incorrecto. Te quedan " + remaining + " intento(s).");
            } else {
                resetCode.setUsedAt(LocalDateTime.now());
                passwordResetCodeRepository.save(resetCode);
                throw new BadRequestException("Has superado el límite de 5 intentos fallidos permitidos. Solicitá un nuevo código.");
            }
        }

        // Codigo correcto -> Marcar como consumido y actualizar password
        resetCode.setUsedAt(LocalDateTime.now());
        passwordResetCodeRepository.save(resetCode);

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("Contraseña restablecida exitosamente via OTP para {}", cleanEmail);

        return MessageResponseDto.builder()
                .message("Contraseña restablecida exitosamente. Ya podés iniciar sesión con tu nueva clave.")
                .build();
    }
}
