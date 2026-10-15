package com.eventsphere.modules.attendee;

import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "attendees", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendees_event_reg", columnNames = {"event_id", "registration_id"})
})
public class AttendeeEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 32)
    private String eventId;

    @Column(name = "registration_id", nullable = false, length = 64)
    private String registrationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 32)
    private String mobile;

    @Column(name = "normalized_mobile", nullable = false, length = 20)
    private String normalizedMobile;

    private String designation;
    private String company;

    @Convert(converter = com.eventsphere.modules.attendee.model.AttendeeCategoryConverter.class)
    @Column(nullable = false, length = 32)
    private AttendeeCategory category = AttendeeCategory.DELEGATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_role", nullable = false, length = 32)
    private UserRole authRole = UserRole.ROLE_DELEGATE;

    @Column(name = "ticket_type", nullable = false, length = 100)
    private String ticketType = "Delegate";

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "relevance_score")
    private Integer relevanceScore = 0;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn = false;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToOne(mappedBy = "attendee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PrivacyConsentEntity privacyConsent;

    public AttendeeEntity() {}

    @PrePersist
    public void onPersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (normalizedMobile == null && mobile != null) {
            String digits = mobile.replaceAll("\\D", "");
            if (digits.startsWith("91") && digits.length() > 10) digits = digits.substring(2);
            if (digits.startsWith("0")) digits = digits.substring(1);
            this.normalizedMobile = digits;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getNormalizedMobile() { return normalizedMobile; }
    public void setNormalizedMobile(String normalizedMobile) { this.normalizedMobile = normalizedMobile; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public AttendeeCategory getCategory() { return category; }
    public void setCategory(AttendeeCategory category) { this.category = category; }

    public UserRole getAuthRole() { return authRole; }
    public void setAuthRole(UserRole authRole) { this.authRole = authRole; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getTwitterUrl() { return twitterUrl; }
    public void setTwitterUrl(String twitterUrl) { this.twitterUrl = twitterUrl; }

    public Integer getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Integer relevanceScore) { this.relevanceScore = relevanceScore; }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

    public Instant getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public PrivacyConsentEntity getPrivacyConsent() { return privacyConsent; }
    public void setPrivacyConsent(PrivacyConsentEntity privacyConsent) { this.privacyConsent = privacyConsent; }
}
