package com.tropelcare.signal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignalRequestDTO(
        @NotNull Long tropelId,
        @NotNull Long guardianId,
        @NotBlank String senderTag,
        @NotBlank @Size(min = 10) String rawContent
) {}
