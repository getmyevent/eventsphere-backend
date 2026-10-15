package com.eventsphere.modules.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpvoteResponse(
        String questionId,
        int upvotes,
        @JsonProperty("hasUpvoted") boolean hasUpvoted,
        String message
) {}
