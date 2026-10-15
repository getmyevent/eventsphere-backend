package com.eventsphere.modules.exhibitor;

import java.io.Serializable;
import java.util.Objects;

public class SavedExhibitorId implements Serializable {

    private String attendeeId;
    private String targetExhibitorId;

    public SavedExhibitorId() {}

    public SavedExhibitorId(String attendeeId, String targetExhibitorId) {
        this.attendeeId = attendeeId;
        this.targetExhibitorId = targetExhibitorId;
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getTargetExhibitorId() { return targetExhibitorId; }
    public void setTargetExhibitorId(String targetExhibitorId) { this.targetExhibitorId = targetExhibitorId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedExhibitorId that = (SavedExhibitorId) o;
        return Objects.equals(attendeeId, that.attendeeId) && Objects.equals(targetExhibitorId, that.targetExhibitorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendeeId, targetExhibitorId);
    }
}
