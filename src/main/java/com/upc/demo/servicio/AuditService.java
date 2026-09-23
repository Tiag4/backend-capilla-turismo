package com.upc.demo.servicio;

import com.upc.demo.dto.audit.AuditEventResponseDto;
import com.upc.demo.entidad.AuditEvent;
import com.upc.demo.repositorio.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public AuditEventResponseDto logEvent(String actionType,
                                          String description,
                                          String operatorName,
                                          String operatorEmail,
                                          String targetEntity,
                                          String targetId,
                                          String details) {
        AuditEvent event = AuditEvent.builder()
                .actionType(actionType)
                .description(description)
                .operatorName(operatorName)
                .operatorEmail(operatorEmail)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        AuditEvent saved = auditEventRepository.save(event);
        return AuditEventResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponseDto> getLogs(String actionType, String query) {
        String cleanAction = (actionType != null && !actionType.equalsIgnoreCase("ALL") && !actionType.isBlank())
                ? actionType.trim()
                : null;
        String cleanQuery = (query != null && !query.isBlank()) ? query.trim() : null;

        List<AuditEvent> events;
        if (cleanAction == null && cleanQuery == null) {
            events = auditEventRepository.findAllByOrderByTimestampDesc();
        } else {
            events = auditEventRepository.searchLogs(cleanAction, cleanQuery);
        }

        return events.stream()
                .map(AuditEventResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
