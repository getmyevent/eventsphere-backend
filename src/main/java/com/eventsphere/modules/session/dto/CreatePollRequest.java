package com.eventsphere.modules.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePollRequest(
        @NotBlank(message = "Session ID is required")
        String sessionId,

        @NotBlank(message = "Question is required")
        String question,

        @NotEmpty(message = "At least 2 options are required")
        @Size(min = 2, max = 10, message = "Poll must contain between 2 and 10 options")
        List<String> options
) {}
