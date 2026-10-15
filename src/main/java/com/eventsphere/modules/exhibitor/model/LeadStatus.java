package com.eventsphere.modules.exhibitor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeadStatus
{
    NEW("New"),
    INTERESTED("Interested"),
    FOLLOW_UP("Follow-up");

    private final String value;

    LeadStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LeadStatus fromValue(String value) {
        if (value == null) return NEW;
        for (LeadStatus status : values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value.replace("-", "_"))) {
                return status;
            }
        }
        return NEW;
    }
}
