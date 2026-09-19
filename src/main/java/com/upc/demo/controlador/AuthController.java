package com.upc.demo.controlador;

import com.upc.demo.config.UserPrincipal;
import com.upc.demo.config.exception.UnauthorizedException;
import com.upc.demo.dto.auth.AuthResponseDto;
import com.upc.demo.dto.auth.LoginRequestDto;
import com.upc.demo.dto.auth.RegisterTouristDto;
import com.upc.demo.dto.auth.UserProfileDto;
import com.upc.demo.servicio.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/register-tourist")
    public ResponseEntity<AuthResponseDto> registerTourist(@Valid @RequestBody RegisterTouristDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerTourist(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Debes estar autenticado para consultar tu perfil");
        }
        return ResponseEntity.ok(authService.getProfile(principal.getId()));
    }
}