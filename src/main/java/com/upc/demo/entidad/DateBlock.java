package com.upc.demo.entidad;

import com.upc.demo.entidad.enums.DateBlockReason;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "date_blocks",
    indexes = {
        @Index(name = "idx_date_block_accommodation_dates", columnList = "accommodation_id, start_date, end_date"),
        @Index(name = "idx_date_block_room_type", columnList = "room_type_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(name = "units_blocked", nullable = false)
    @Builder.Default
    private Integer unitsBlocked = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DateBlockReason reason = DateBlockReason.MAINTENANCE;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.reason == null) {
            this.reason = DateBlockReason.MAINTENANCE;
        }
        if (this.unitsBlocked == null || this.unitsBlocked < 1) {
            this.unitsBlocked = 1;
        }
    }

    public Integer getUnitsBlocked() {
        return this.unitsBlocked != null ? this.unitsBlocked : 1;
    }
}
