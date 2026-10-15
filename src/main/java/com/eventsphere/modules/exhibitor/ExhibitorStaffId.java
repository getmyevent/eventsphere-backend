package com.eventsphere.modules.exhibitor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExhibitorStaffId implements Serializable {

    @Column(name = "exhibitor_id", nullable = false, length = 64)
    private String exhibitorId;

    @Column(name = "attendee_id", nullable = false, length = 64)
    private String attendeeId;

    public ExhibitorStaffId() {}

    public ExhibitorStaffId(String exhibitorId, String attendeeId) {
        this.exhibitorId = exhibitorId;
        this.attendeeId = attendeeId;
    }

    public String getExhibitorId() {
        return exhibitorId;
    }

    public void setExhibitorId(String exhibitorId) {
        this.exhibitorId = exhibitorId;
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExhibitorStaffId that = (ExhibitorStaffId) o;
        return Objects.equals(exhibitorId, that.exhibitorId) &&
               Objects.equals(attendeeId, that.attendeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exhibitorId, attendeeId);
    }
}
