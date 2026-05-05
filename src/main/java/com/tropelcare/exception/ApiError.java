package com.tropelcare.exception;

import java.time.Instant;

public record ApiError(
        String error,
        String message,
        Instant timestamp,
        String path
) {}
