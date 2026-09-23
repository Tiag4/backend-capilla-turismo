package com.upc.demo.servicio;

import com.upc.demo.config.exception.BadRequestException;
import com.upc.demo.config.exception.ConflictException;
import com.upc.demo.config.exception.ResourceNotFoundException;
import com.upc.demo.dto.invitation.CreateInvitationDto;
import com.upc.demo.dto.invitation.InvitationResponseDto;
import com.upc.demo.dto.invitation.ValidateTokenResponseDto;
import com.upc.demo.entidad.InvitationToken;
import com.upc.demo.entidad.User;
import com.upc.demo.repositorio.InvitationTokenRepository;
import com.upc.demo.repositorio.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationTokenRepository invitationTokenRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public InvitationResponseDto createInvitation(CreateInvitationDto dto, UUID adminId) {
        String cleanEmail = dto.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ConflictException("Ya existe un usuario registrado con el email: " + cleanEmail);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado con ID: " + adminId));

        String token = "inv_" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        InvitationToken invitation = InvitationToken.builder()
                .token(token)
                .email(cleanEmail)
                .expiresAt(expiresAt)
                .createdBy(admin)
                .build();

        InvitationToken saved = invitationTokenRepository.save(invitation);

        auditService.logEvent(
                "TOKEN_GENERATED",
                "Emisión de token de seguridad para registro de prestador: " + cleanEmail,
                admin.getName() + " " + admin.getLastName(),
                admin.getEmail(),
                cleanEmail,
                saved.getId().toString(),
                "Token con vigencia de 7 días generado para incorporación al padrón oficial."
        );

        return InvitationResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getAllInvitations() {
        return invitationTokenRepository.findAll().stream()
                .map(InvitationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ValidateTokenResponseDto validateToken(String token) {
        InvitationToken invitation = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de invitacion no encontrado"));

        if (invitation.getUsedAt() != null) {
            throw new BadRequestException("Este token de invitacion ya fue utilizado");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token de invitacion ha expirado");
        }

        return ValidateTokenResponseDto.builder()
                .valid(true)
                .email(invitation.getEmail())
                .expiresAt(invitation.getExpiresAt())
                .message("Token de invitacion valido")
                .build();
    }

    @Transactional
    public void deleteInvitation(UUID id, com.upc.demo.config.UserPrincipal admin) {
        InvitationToken invitation = invitationTokenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Token de invitacion no encontrado con ID: " + id));
        if (invitation.getUsedAt() != null) {
            throw new BadRequestException("No se puede revocar un token que ya fue utilizado");
        }
        String email = invitation.getEmail();
        invitationTokenRepository.delete(invitation);

        String opName = admin != null ? admin.getUsername() : "Administrador";
        auditService.logEvent(
                "TOKEN_REVOKED",
                "Revocación de token de invitación emitido para: " + email,
                opName,
                admin != null ? admin.getUsername() : "admin@capilladelmonte.gov.ar",
                email,
                id.toString(),
                "Token eliminado preventivamente por la administración municipal."
        );
    }
}