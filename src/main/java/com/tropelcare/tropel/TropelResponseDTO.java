package com.tropelcare.tropel;

import com.tropelcare.common.Species;
import com.tropelcare.common.VitalState;

import java.time.Instant;

public record TropelResponseDTO(
        Long id,
        String name,
        Species species,
        VitalState vitalState,
        Integer energyLevel,
        Integer chaosIndex,
        Integer mutationStage,
        Long sectorId,
        String sectorCode,
        Long guardianId,
        String guardianName,
        Instant createdAt,
        Instant updatedAt
) {}
