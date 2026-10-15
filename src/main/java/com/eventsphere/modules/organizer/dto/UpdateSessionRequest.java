package com.eventsphere.modules.organizer.dto;

import java.util.List;

public record UpdateSessionRequest(
        String title,
        String track,
        Integer day,
        String timeRange,
        String room,
        String status,
        String speakerName,
        String speakerDesignation,
        String speakerCompany,
        String description,
        List<String> takeaways
) {}
