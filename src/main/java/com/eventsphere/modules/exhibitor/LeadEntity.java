package com.eventsphere.modules.exhibitor;

import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.exhibitor.model.LeadStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "leads", uniqueConstraints = {
        @UniqueConstraint(name = "uk_leads_exhibitor_attendee", columnNames = {"exhibitor_id", "attendee_id"})
})
public class LeadEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "exhibitor_id", nullable = false, length = 64)
    private String exhibitorId;

    @Column(name = "attendee_id", nullable = false, length = 64)
    private String attendeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibitor_id", insertable = false, updatable = false)
    private ExhibitorEntity exhibitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", insertable = false, updatable = false)
    private AttendeeEntity attendee;

    @Convert(converter = com.eventsphere.modules.exhibitor.model.LeadStatusConverter.class)
    @Column(nullable = false, length = 32)
    private LeadStatus status = LeadStatus.NEW;

    @Column(nullable = false, length = 100)
    private String source = "Directory Lead Capture";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "interests_json", columnDefinition = "JSON")
    private String interestsJson;

    @Column(name = "products_viewed_json", columnDefinition = "JSON")
    private String productsViewedJson;

    @Column(name = "sync_status", nullable = false, length = 32)
    private String syncStatus = "synced";

    @Column(name = "captured_at", nullable = false, updatable = false)
    private Instant capturedAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public LeadEntity() {}

    public LeadEntity(String id, String exhibitorId, String attendeeId, LeadStatus status, String source, String notes) {
        this.id = id;
        this.exhibitorId = exhibitorId;
        this.attendeeId = attendeeId;
        this.status = status != null ? status : LeadStatus.NEW;
        this.source = source != null ? source : "Directory Lead Capture";
        this.notes = notes;
        this.capturedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void onPersist() {
        if (capturedAt == null) capturedAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (status == null) status = LeadStatus.NEW;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExhibitorId() { return exhibitorId; }
    public void setExhibitorId(String exhibitorId) { this.exhibitorId = exhibitorId; }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public ExhibitorEntity getExhibitor() { return exhibitor; }
    public void setExhibitor(ExhibitorEntity exhibitor) { this.exhibitor = exhibitor; }

    public AttendeeEntity getAttendee() { return attendee; }
    public void setAttendee(AttendeeEntity attendee) {
        this.attendee = attendee;
        if (attendee != null) {
            this.attendeeId = attendee.getId();
        }
    }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInterestsJson() { return interestsJson; }
    public void setInterestsJson(String interestsJson) { this.interestsJson = interestsJson; }

    public String getProductsViewedJson() { return productsViewedJson; }
    public void setProductsViewedJson(String productsViewedJson) { this.productsViewedJson = productsViewedJson; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public Instant getCapturedAt() { return capturedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
