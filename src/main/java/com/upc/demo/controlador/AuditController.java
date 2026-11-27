package com.upc.demo.controlador;

import com.upc.demo.dto.audit.AuditEventResponseDto;
import com.upc.demo.servicio.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditEventResponseDto>> getLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(auditService.getLogs(actionType, search));
    }
}
