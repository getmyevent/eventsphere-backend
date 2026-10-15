package com.eventsphere.modules.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(
        @NotBlank(message = "Question text cannot be blank")
        @Size(max = 2000, message = "Question text must not exceed 2000 characters")
        String text,

        @JsonProperty("isAnonymous")
        boolean isAnonymous
) {}
