package com.eventsphere.modules.auth.dto;

public record GooglePayload(
        String email,
        boolean emailVerified,
        String name,
        String pictureUrl,
        String subject
) {}
