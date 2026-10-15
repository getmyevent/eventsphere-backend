package com.eventsphere.modules.organizer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WalkInRegistrationRequest(
        @NotBlank(message = "Attendee name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email address is required")
        String email,

        @NotBlank(message = "Mobile number is required")
        String mobile,

        @NotBlank(message = "Company is required")
        String company,

        @NotBlank(message = "Designation is required")
        String designation,

        String ticketType,

        String category
) {}
