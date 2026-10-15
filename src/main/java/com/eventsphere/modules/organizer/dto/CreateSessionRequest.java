package com.eventsphere.modules.organizer.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateSessionRequest(
        @NotBlank(message = "Session title is required")
        String title,

        String track,
        int day,
        String timeRange,
        String room,
        String status,
        String speakerName,
        String speakerDesignation,
        String speakerCompany,
        String description,
        List<String> takeaways
) {}
