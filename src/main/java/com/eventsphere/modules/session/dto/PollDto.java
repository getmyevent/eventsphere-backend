package com.eventsphere.modules.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PollDto(
        String id,
        String sessionId,
        String question,
        String status,
        int totalVotes,
        List<PollOptionDto> options,
        @JsonProperty("hasVoted") boolean hasVoted,
        String votedOptionId,
        @JsonProperty("isActive") boolean isActive
) {}
