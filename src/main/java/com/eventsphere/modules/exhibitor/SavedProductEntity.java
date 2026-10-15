package com.eventsphere.modules.exhibitor;

import com.eventsphere.modules.attendee.AttendeeEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "saved_products")
@IdClass(SavedProductId.class)
public class SavedProductEntity {

    @Id
    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    @Id
    @Column(name = "target_product_id", length = 64)
    private String targetProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", insertable = false, updatable = false)
    private AttendeeEntity attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id", insertable = false, updatable = false)
    private ProductEntity targetProduct;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SavedProductEntity() {}

    public SavedProductEntity(String attendeeId, String targetProductId) {
        this.attendeeId = attendeeId;
        this.targetProductId = targetProductId;
        this.createdAt = Instant.now();
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getTargetProductId() { return targetProductId; }
    public void setTargetProductId(String targetProductId) { this.targetProductId = targetProductId; }

    public AttendeeEntity getAttendee() { return attendee; }
    public ProductEntity getTargetProduct() { return targetProduct; }

    public Instant getCreatedAt() { return createdAt; }
}
