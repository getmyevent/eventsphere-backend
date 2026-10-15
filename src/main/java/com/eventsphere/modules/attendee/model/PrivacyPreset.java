package com.eventsphere.modules.attendee.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PrivacyPreset {
    OPEN("Open"),
    CONNECTIONS_ONLY("Connections Only");

    private final String value;

    PrivacyPreset(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PrivacyPreset fromValue(String value) {
        if (value == null) return CONNECTIONS_ONLY;
        for (PrivacyPreset preset : values()) {
            if (preset.value.equalsIgnoreCase(value) || preset.name().equalsIgnoreCase(value.replace(" ", "_"))) {
                return preset;
            }
        }
        return CONNECTIONS_ONLY;
    }
}
