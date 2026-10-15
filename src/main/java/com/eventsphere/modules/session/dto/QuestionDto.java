package com.eventsphere.modules.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuestionDto(
        String id,
        String sessionId,
        String text,
        String authorName,
        String authorId,
        @JsonProperty("isAnonymous") boolean isAnonymous,
        String status,
        int upvotes,
        @JsonProperty("hasUpvoted") boolean hasUpvoted,
        String createdAt
) {}
