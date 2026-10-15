package com.eventsphere.modules.session.dto;

public record PollOptionDto(
        String id,
        String text,
        int votes,
        int sortOrder
) {}
