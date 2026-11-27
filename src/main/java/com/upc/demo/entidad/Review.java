package com.upc.demo.entidad;

import com.upc.demo.entidad.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_booking", columnNames = "booking_id")
    },
    indexes = {
        @Index(name = "idx_review_accommodation_status", columnList = "accommodation_id, status"),
        @Index(name = "idx_review_status_created", columnList = "status, created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(name = "booking_code", nullable = false, length = 30)
    private String bookingCode;

    @Column(name = "guest_name", nullable = false, length = 150)
    private String guestName;

    @Column(name = "guest_origin", length = 100)
    private String guestOrigin;

    @Column(name = "stay_date", length = 60)
    private String stayDate;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "cleanliness_rating", nullable = false)
    private Integer cleanlinessRating;

    @Column(name = "accuracy_rating")
    @Builder.Default
    private Integer accuracyRating = 5;

    @Column(name = "check_in_rating")
    @Builder.Default
    private Integer checkInRating = 5;

    @Column(name = "host_rating", nullable = false)
    private Integer hostRating;

    @Column(name = "location_rating", nullable = false)
    private Integer locationRating;

    @Column(name = "value_rating")
    @Builder.Default
    private Integer valueRating = 5;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReviewStatus.PENDING;
        }
        if (this.accuracyRating == null) {
            this.accuracyRating = this.rating != null ? this.rating : 5;
        }
        if (this.checkInRating == null) {
            this.checkInRating = this.rating != null ? this.rating : 5;
        }
        if (this.valueRating == null) {
            this.valueRating = this.rating != null ? this.rating : 5;
        }
    }

    public Integer getAccuracyRating() {
        return this.accuracyRating != null ? this.accuracyRating : (this.rating != null ? this.rating : 5);
    }

    public Integer getCheckInRating() {
        return this.checkInRating != null ? this.checkInRating : (this.rating != null ? this.rating : 5);
    }

    public Integer getValueRating() {
        return this.valueRating != null ? this.valueRating : (this.rating != null ? this.rating : 5);
    }
}
