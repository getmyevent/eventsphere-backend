package com.eventsphere.modules.session.dto;

import jakarta.validation.constraints.NotBlank;

public record VotePollRequest(
        @NotBlank(message = "Option ID is required")
        String optionId
) {}
