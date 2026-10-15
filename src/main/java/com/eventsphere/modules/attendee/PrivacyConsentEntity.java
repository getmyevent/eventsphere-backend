package com.eventsphere.modules.attendee;

import com.eventsphere.modules.attendee.model.PrivacyPreset;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "privacy_consents")
public class PrivacyConsentEntity {

    @Id
    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "attendee_id")
    private AttendeeEntity attendee;

    @Convert(converter = com.eventsphere.modules.attendee.model.PrivacyPresetConverter.class)
    @Column(nullable = false, length = 32)
    private PrivacyPreset preset = PrivacyPreset.CONNECTIONS_ONLY;

    @Column(name = "share_email", nullable = false)
    private boolean shareEmail = true;

    @Column(name = "share_phone", nullable = false)
    private boolean sharePhone = true;

    @Column(name = "share_linkedin", nullable = false)
    private boolean shareLinkedIn = true;

    @Column(name = "share_twitter", nullable = false)
    private boolean shareTwitter = false;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PrivacyConsentEntity() {}

    public PrivacyConsentEntity(String attendeeId, PrivacyPreset preset, boolean shareEmail, boolean sharePhone, boolean shareLinkedIn, boolean shareTwitter) {
        this.attendeeId = attendeeId;
        this.preset = preset;
        this.shareEmail = shareEmail;
        this.sharePhone = sharePhone;
        this.shareLinkedIn = shareLinkedIn;
        this.shareTwitter = shareTwitter;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public AttendeeEntity getAttendee() { return attendee; }
    public void setAttendee(AttendeeEntity attendee) { this.attendee = attendee; }

    public PrivacyPreset getPreset() { return preset; }
    public void setPreset(PrivacyPreset preset) { this.preset = preset; }

    public boolean isShareEmail() { return shareEmail; }
    public void setShareEmail(boolean shareEmail) { this.shareEmail = shareEmail; }

    public boolean isSharePhone() { return sharePhone; }
    public void setSharePhone(boolean sharePhone) { this.sharePhone = sharePhone; }

    public boolean isShareLinkedIn() { return shareLinkedIn; }
    public void setShareLinkedIn(boolean shareLinkedIn) { this.shareLinkedIn = shareLinkedIn; }

    public boolean isShareTwitter() { return shareTwitter; }
    public void setShareTwitter(boolean shareTwitter) { this.shareTwitter = shareTwitter; }

    public Instant getUpdatedAt() { return updatedAt; }
}
