package com.eventsphere.modules.attendee.dto;

import com.eventsphere.modules.attendee.model.PrivacyPreset;
import jakarta.validation.constraints.NotNull;

public class UpdatePrivacyRequest {

    @NotNull(message = "Privacy preset is required")
    private PrivacyPreset preset;

    private Boolean shareEmail;
    private Boolean sharePhone;
    private Boolean shareLinkedIn;
    private Boolean shareTwitter;

    public UpdatePrivacyRequest() {}

    public UpdatePrivacyRequest(PrivacyPreset preset, Boolean shareEmail, Boolean sharePhone, Boolean shareLinkedIn, Boolean shareTwitter) {
        this.preset = preset;
        this.shareEmail = shareEmail;
        this.sharePhone = sharePhone;
        this.shareLinkedIn = shareLinkedIn;
        this.shareTwitter = shareTwitter;
    }

    public PrivacyPreset getPreset() { return preset; }
    public void setPreset(PrivacyPreset preset) { this.preset = preset; }

    public Boolean getShareEmail() { return shareEmail; }
    public void setShareEmail(Boolean shareEmail) { this.shareEmail = shareEmail; }

    public Boolean getSharePhone() { return sharePhone; }
    public void setSharePhone(Boolean sharePhone) { this.sharePhone = sharePhone; }

    public Boolean getShareLinkedIn() { return shareLinkedIn; }
    public void setShareLinkedIn(Boolean shareLinkedIn) { this.shareLinkedIn = shareLinkedIn; }

    public Boolean getShareTwitter() { return shareTwitter; }
    public void setShareTwitter(Boolean shareTwitter) { this.shareTwitter = shareTwitter; }
}
