package com.upc.demo.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "operator_name", nullable = false, length = 150)
    private String operatorName;

    @Column(name = "operator_email", nullable = false, length = 150)
    private String operatorEmail;

    @Column(name = "target_entity", length = 150)
    private String targetEntity;

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
