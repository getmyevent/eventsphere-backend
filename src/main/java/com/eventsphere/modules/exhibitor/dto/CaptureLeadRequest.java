package com.eventsphere.modules.exhibitor.dto;

import com.eventsphere.modules.exhibitor.model.LeadStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CaptureLeadRequest {

    @NotBlank(message = "Attendee ID is required")
    private String attendeeId;

    private LeadStatus status = LeadStatus.NEW;

    private String source = "Directory Lead Capture";

    private String notes;

    private List<String> interests;

    public CaptureLeadRequest() {}

    public CaptureLeadRequest(String attendeeId, LeadStatus status, String source, String notes, List<String> interests) {
        this.attendeeId = attendeeId;
        this.status = status != null ? status : LeadStatus.NEW;
        this.source = source != null && !source.isBlank() ? source : "Directory Lead Capture";
        this.notes = notes;
        this.interests = interests;
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status != null ? status : LeadStatus.NEW; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source != null && !source.isBlank() ? source : "Directory Lead Capture"; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}
