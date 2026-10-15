package com.eventsphere.modules.attendee.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AttendeeCategory {
    DELEGATE("delegate"),
    SPEAKER("speaker"),
    VIP("vip"),
    GUEST("guest"),
    EXHIBITOR_STAFF("exhibitor_staff"),
    ORGANIZER_STAFF("organizer_staff");

    private final String value;

    AttendeeCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static AttendeeCategory fromValue(String value) {
        for (AttendeeCategory category : values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        return DELEGATE;
    }
}
