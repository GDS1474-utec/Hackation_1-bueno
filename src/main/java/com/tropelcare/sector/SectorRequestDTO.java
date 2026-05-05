package com.tropelcare.sector;

import com.tropelcare.common.Climate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectorRequestDTO(
        @NotBlank String sectorCode,
        @NotNull Climate climate,
        @NotNull @Min(1) @Max(100000) Integer capacity
) {}
