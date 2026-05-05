package com.tropelcare.tropel;

import com.tropelcare.common.Species;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TropelRequestDTO(
        @NotBlank @Size(min = 2, max = 40) String name,
        @NotNull Species species,
        @NotNull Long sectorId,
        @NotNull Long guardianId
) {}
