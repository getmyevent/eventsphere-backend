package com.eventsphere.modules.exhibitor;

import jakarta.persistence.*;

@Entity
@Table(name = "exhibitor_staff")
public class ExhibitorStaffEntity {

    @EmbeddedId
    private ExhibitorStaffId id;

    @Column(name = "event_id", nullable = false, length = 32)
    private String eventId;

    @Column(name = "is_booth_admin", nullable = false)
    private boolean boothAdmin = false;

    public ExhibitorStaffEntity() {}

    public ExhibitorStaffEntity(String eventId, String exhibitorId, String attendeeId, boolean boothAdmin) {
        this.id = new ExhibitorStaffId(exhibitorId, attendeeId);
        this.eventId = eventId;
        this.boothAdmin = boothAdmin;
    }

    public ExhibitorStaffId getId() {
        return id;
    }

    public void setId(ExhibitorStaffId id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isBoothAdmin() {
        return boothAdmin;
    }

    public void setBoothAdmin(boolean boothAdmin) {
        this.boothAdmin = boothAdmin;
    }
}
