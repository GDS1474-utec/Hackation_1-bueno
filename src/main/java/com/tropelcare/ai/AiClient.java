package com.tropelcare.ai;

public interface AiClient {
    AiClassificationResult classify(String rawContent);
}
