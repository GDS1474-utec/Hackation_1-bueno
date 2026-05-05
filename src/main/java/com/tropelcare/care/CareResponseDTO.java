package com.tropelcare.care;

import java.time.Instant;

public record CareResponseDTO(
        Long id,
        Long signalId,
        String responseCode,
        String description,
        Instant createdAt
) {}
