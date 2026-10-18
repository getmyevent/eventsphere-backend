package com.eventsphere.modules.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePollStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "(?i)^(open|closed)$", message = "Status must be either 'open' or 'closed'")
        String status
) {}
