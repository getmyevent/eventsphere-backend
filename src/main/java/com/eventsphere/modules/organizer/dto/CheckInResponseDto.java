package com.eventsphere.modules.organizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CheckInResponseDto(
        String attendeeId,
        String name,
        @JsonProperty("checkedIn") boolean checkedIn,
        String checkInTime,
        String message
) {}
