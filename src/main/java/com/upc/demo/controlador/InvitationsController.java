package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.dto.invitation.CreateInvitationDto;
import com.upc.demo.dto.invitation.InvitationResponseDto;
import com.upc.demo.dto.invitation.ValidateTokenResponseDto;
import com.upc.demo.servicio.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationsController {

    private final InvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponseDto> createInvitation(
            @Valid @RequestBody CreateInvitationDto dto,
            @AuthenticationPrincipal UserPrincipal admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.createInvitation(dto, admin.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationResponseDto>> getAllInvitations() {
        return ResponseEntity.ok(invitationService.getAllInvitations());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInvitation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal admin) {
        invitationService.deleteInvitation(id, admin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<ValidateTokenResponseDto> validateToken(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.validateToken(token));
    }
}