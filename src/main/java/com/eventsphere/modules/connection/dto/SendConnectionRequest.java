package com.eventsphere.modules.connection.dto;

import jakarta.validation.constraints.NotBlank;

public class SendConnectionRequest {

    @NotBlank(message = "Target attendee ID is required")
    private String targetAttendeeId;

    private String source = "direct"; // 'direct', 'qr_scan', 'recommended'

    public SendConnectionRequest() {}

    public SendConnectionRequest(String targetAttendeeId, String source) {
        this.targetAttendeeId = targetAttendeeId;
        this.source = source != null && !source.isBlank() ? source : "direct";
    }

    public String getTargetAttendeeId() { return targetAttendeeId; }
    public void setTargetAttendeeId(String targetAttendeeId) { this.targetAttendeeId = targetAttendeeId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source != null && !source.isBlank() ? source : "direct"; }
}
