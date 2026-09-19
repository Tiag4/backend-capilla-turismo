package com.upc.demo.servicio;

import com.upc.demo.config.JwtTokenProvider;
import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.auth.AuthResponseDto;
import com.upc.demo.dto.auth.LoginRequestDto;
import com.upc.demo.dto.auth.RegisterHostDto;
import com.upc.demo.dto.auth.RegisterTouristDto;
import com.upc.demo.dto.auth.UserProfileDto;
import com.upc.demo.entidad.InvitationToken;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.InvitationTokenRepository;
import com.upc.demo.repositorio.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto dto) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
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
}