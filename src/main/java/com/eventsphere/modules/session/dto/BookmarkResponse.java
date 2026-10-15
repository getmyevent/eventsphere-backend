package com.eventsphere.modules.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookmarkResponse(
        String sessionId,
        @JsonProperty("isBookmarked") boolean isBookmarked,
        String message
) {}
