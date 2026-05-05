package com.tropelcare.ai;

import com.tropelcare.common.Severity;
import com.tropelcare.common.SignalType;

public record AiClassificationResult(
        SignalType signalType,
        Severity severity,
        String assignedUnit,
        String recommendedAction,
        boolean fallback
) {
    public static AiClassificationResult fallbackResult() {
        return new AiClassificationResult(
                SignalType.SENAL_CORRUPTA,
                Severity.LEVE,
                "Archivo de Senales",
                "Archivar la señal y revisar manualmente si se repite.",
                true
        );
    }
}
