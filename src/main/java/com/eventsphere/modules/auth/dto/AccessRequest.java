package com.eventsphere.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

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
