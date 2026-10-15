package com.eventsphere.modules.exhibitor.dto;

import com.eventsphere.modules.exhibitor.model.LeadStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateLeadStatusRequest {

    @NotNull(message = "Lead status is required")
    private LeadStatus status;

    private String notes;

    public UpdateLeadStatusRequest() {}

    public UpdateLeadStatusRequest(LeadStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
