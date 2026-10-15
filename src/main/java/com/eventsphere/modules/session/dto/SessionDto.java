package com.eventsphere.modules.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SessionDto(
        String id,
        String title,
        String track,
        int day,
        String timeRange,
        String room,
        String sessionType,
        String speakerName,
        String speakerDesignation,
        String speakerCompany,
        String speakerId,
        String description,
        List<String> takeaways,
        String status,
        int attendanceCount,
        int questionsCount,
        @JsonProperty("isBookmarked") boolean isBookmarked
) {}
