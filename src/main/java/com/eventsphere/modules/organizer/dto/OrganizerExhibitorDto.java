package com.eventsphere.modules.organizer.dto;

public record OrganizerExhibitorDto(
        String id,
        String company,
        String booth,
        String category,
        String logoUrl,
        long leadCount
) {}
