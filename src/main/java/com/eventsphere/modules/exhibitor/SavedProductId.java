package com.eventsphere.modules.exhibitor;

import java.io.Serializable;
import java.util.Objects;

public class SavedProductId implements Serializable {

    private String attendeeId;
    private String targetProductId;

    public SavedProductId() {}

    public SavedProductId(String attendeeId, String targetProductId) {
        this.attendeeId = attendeeId;
        this.targetProductId = targetProductId;
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getTargetProductId() { return targetProductId; }
    public void setTargetProductId(String targetProductId) { this.targetProductId = targetProductId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedProductId that = (SavedProductId) o;
        return Objects.equals(attendeeId, that.attendeeId) && Objects.equals(targetProductId, that.targetProductId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attendeeId, targetProductId);
    }
}
