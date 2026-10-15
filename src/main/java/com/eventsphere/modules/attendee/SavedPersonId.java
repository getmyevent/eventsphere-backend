package com.eventsphere.modules.attendee;

import java.io.Serializable;
import java.util.Objects;

public class SavedPersonId implements Serializable {

    private String attendeeId;
    private String targetPersonId;

    public SavedPersonId() {}

    public SavedPersonId(String attendeeId, String targetPersonId) {
        this.attendeeId = attendeeId;
        this.targetPersonId = targetPersonId;
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getTargetPersonId() { return targetPersonId; }
    public void setTargetPersonId(String targetPersonId) { this.targetPersonId = targetPersonId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedPersonId that = (SavedPersonId) o;
        return Objects.equals(attendeeId, that.attendeeId) && Objects.equals(targetPersonId, that.targetPersonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendeeId, targetPersonId);
    }
}
