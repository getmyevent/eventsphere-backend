package com.eventsphere.modules.organizer.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSessionStatusRequest(
        @NotBlank(message = "Session status is required")
        String status
) {}
