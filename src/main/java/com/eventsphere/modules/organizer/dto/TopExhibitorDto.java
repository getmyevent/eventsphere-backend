package com.eventsphere.modules.organizer.dto;

import java.util.List;

public record TopExhibitorDto(
        String id,
        String name,
        String booth,
        String category,
        long leads
) {}
