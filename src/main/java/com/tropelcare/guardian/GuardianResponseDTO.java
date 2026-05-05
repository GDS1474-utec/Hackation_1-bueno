package com.tropelcare.guardian;

import java.time.Instant;

public record GuardianResponseDTO(
        Long id,
        String displayName,
        String email,
        String notificationEmail,
        Instant createdAt
) {}
