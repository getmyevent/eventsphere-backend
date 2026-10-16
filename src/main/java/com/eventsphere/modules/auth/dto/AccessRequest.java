package com.eventsphere.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Authentication request payload.
 *
 * TODO [SECURITY / AUTH ARCHITECTURE NOTE]:
 * The current event check-in/login model authenticates attendees via verified mobile number
 * (frictionless on-site login for ~5,000 users without SMS OTP gateway dependencies).
 * Anyone possessing a registered attendee's mobile number could authenticate as that attendee.
 * Production enhancement options for post-launch:
 * 1. Optional SMS OTP verification (via MSG91 / Twilio / Karix).
 * 2. Optional 2-factor verification requiring Registration ID (#REG-...) alongside mobile.
 */
public class AccessRequest {

    @NotBlank(message = "Event ID is required")
    private String eventId;

    @NotBlank(message = "Mobile number is required")
    private String mobile;

    public AccessRequest() {}

    public AccessRequest(String eventId, String mobile) {
        this.eventId = eventId;
        this.mobile = mobile;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
