package com.eventsphere.modules.organizer.dto;

import java.util.List;

public record ImportResultDto(
        int totalProcessed,
        int totalImported,
        int totalSkipped,
        List<String> errors
) {}
