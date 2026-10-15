package com.eventsphere.modules.attendee.dto;

import com.eventsphere.modules.attendee.model.PrivacyPreset;

public class PrivacyConsentDto {

    private PrivacyPreset preset = PrivacyPreset.CONNECTIONS_ONLY;
    private boolean shareEmail = true;
    private boolean sharePhone = true;
    private boolean shareLinkedIn = true;
    private boolean shareTwitter = false;

    public PrivacyConsentDto() {}

    public PrivacyConsentDto(PrivacyPreset preset, boolean shareEmail, boolean sharePhone, boolean shareLinkedIn, boolean shareTwitter) {
        this.preset = preset != null ? preset : PrivacyPreset.CONNECTIONS_ONLY;
        this.shareEmail = shareEmail;
        this.sharePhone = sharePhone;
        this.shareLinkedIn = shareLinkedIn;
        this.shareTwitter = shareTwitter;
    }

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
}
