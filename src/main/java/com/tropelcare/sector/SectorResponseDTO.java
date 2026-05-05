package com.tropelcare.sector;

import com.tropelcare.common.Climate;

import java.time.Instant;

public record SectorResponseDTO(
        Long id,
        String sectorCode,
        Climate climate,
        Integer capacity,
        Integer currentLoad,
        Integer stabilityLevel,
        Instant createdAt
) {}
