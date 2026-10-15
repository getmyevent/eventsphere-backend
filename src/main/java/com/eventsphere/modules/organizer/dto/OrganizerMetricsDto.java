package com.eventsphere.modules.organizer.dto;

import java.util.List;

public record OrganizerMetricsDto(
        long totalRegistered,
        long totalCheckedIn,
        double checkInRate,
        long totalExhibitors,
        long activeExhibitors,
        long totalSpeakers,
        long totalSessions,
        long liveSessions,
        long totalConnections,
        long totalLeads,
        List<TopExhibitorDto> topExhibitors
) {}
