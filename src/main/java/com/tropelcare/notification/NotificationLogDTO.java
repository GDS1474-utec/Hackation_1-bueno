package com.tropelcare.notification;

import com.tropelcare.common.NotificationStatus;
import java.time.Instant;

public record NotificationLogDTO(
        Long id,
        Long signalId,
        String recipientEmail,
        String subject,
        NotificationStatus notifStatus,
        String errorMessage,
        Instant sentAt,
        Instant createdAt
) {}
