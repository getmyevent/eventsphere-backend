package com.eventsphere.modules.exhibitor.dto;

public record PublicExhibitorDto(
        String id,
        String company,
        String booth,
        String category,
        String logoUrl,
        String brochureUrl,
        String brochureName,
        String status
) {}
