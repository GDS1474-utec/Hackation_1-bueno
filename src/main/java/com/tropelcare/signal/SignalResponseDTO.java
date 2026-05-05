package com.tropelcare.signal;

import com.tropelcare.common.Severity;
import com.tropelcare.common.SignalStatus;
import com.tropelcare.common.SignalType;

import java.time.Instant;

public record SignalResponseDTO(
        Long id,
        Long tropelId,
        String tropelName,
        Long guardianId,
        String guardianName,
        String senderTag,
        String rawContent,
        SignalType signalType,
        Severity severity,
        String assignedUnit,
        String recommendedAction,
        SignalStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
