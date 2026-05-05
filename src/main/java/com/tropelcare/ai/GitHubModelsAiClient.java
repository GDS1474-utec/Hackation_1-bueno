package com.tropelcare.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tropelcare.common.Severity;
import com.tropelcare.common.SignalType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GitHubModelsAiClient implements AiClient {

    private static final String SYSTEM_PROMPT = """
            Eres el sistema de clasificación de señales del TropelCare Signal Engine, desarrollado por Tuckersoft.
            Recibes señales emitidas por criaturas digitales llamadas Tropeles y debes clasificarlas.
            Responde ÚNICAMENTE con este JSON en una sola línea, sin texto adicional, sin markdown, sin bloques de código:
            {"signalType":"<TIPO>","severity":"<GRAVEDAD>","assignedUnit":"<UNIDAD>","recommendedAction":"<acción breve y concreta en español>"}

            Tipos válidos: HAMBRE, ABANDONO, MUTACION, FUGA, CONFLICTO, REPRODUCCION_MASIVA, SENAL_CORRUPTA
            Gravedades válidas: LEVE, MODERADO, GRAVE, CRITICO
            Unidades válidas: Laboratorio de Nutricion, Unidad de Bienestar, Division Genetica, Equipo de Contencion, Consejo de Mediacion, Control Demografico, Archivo de Senales

            Reglas:
            - HAMBRE → Laboratorio de Nutricion: escasez de nutrientes, intento de morder objetos digitales.
            - ABANDONO → Unidad de Bienestar: angustia por falta de interacción, silencio prolongado.
            - MUTACION → Division Genetica: cambios físicos, brillo anómalo, glitch corporal, duplicación.
            - FUGA → Equipo de Contencion: intento de abandonar el sector, zonas prohibidas.
            - CONFLICTO → Consejo de Mediacion: pelea entre Tropeles, invasión de territorio.
            - REPRODUCCION_MASIVA → Control Demografico: reproducción no planificada, clonación accidental.
            - SENAL_CORRUPTA → Archivo de Senales: señal ininteligible, estática, datos corruptos.

            Gravedades:
            - LEVE: sin riesgo inmediato para el Tropel o el sector.
            - MODERADO: requiere atención, pero no es urgente.
            - GRAVE: afecta al Tropel o al sector de forma importante.
            - CRITICO: riesgo de mutación irreversible, fuga masiva o colapso del sector.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String token;
    private final String modelId;

    public GitHubModelsAiClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${github.models.url}") String modelsUrl,
            @Value("${github.token}") String token,
            @Value("${github.models.model-id}") String modelId
    ) {
        this.restClient = builder.baseUrl(modelsUrl).build();
        this.objectMapper = objectMapper;
        this.token = token;
        this.modelId = modelId;
    }

    @Override
    public AiClassificationResult classify(String rawContent) {
        try {
            if (token == null || token.isBlank()) {
                return AiClassificationResult.fallbackResult();
            }

            Map<String, Object> body = Map.of(
                    "model", modelId,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", rawContent)
                    ),
                    "temperature", 0
            );

            String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            return parseContent(content);
        } catch (Exception ex) {
            return AiClassificationResult.fallbackResult();
        }
    }

    private AiClassificationResult parseContent(String content) {
        try {
            String json = extractJson(content);
            JsonNode node = objectMapper.readTree(json);

            SignalType signalType = SignalType.valueOf(node.path("signalType").asText());
            Severity severity = Severity.valueOf(node.path("severity").asText());
            String assignedUnit = node.path("assignedUnit").asText();
            String recommendedAction = node.path("recommendedAction").asText();

            if (!expectedUnit(signalType).equals(assignedUnit)) {
                return AiClassificationResult.fallbackResult();
            }

            if (recommendedAction == null || recommendedAction.isBlank()) {
                return AiClassificationResult.fallbackResult();
            }

            return new AiClassificationResult(signalType, severity, assignedUnit, recommendedAction, false);
        } catch (Exception ex) {
            return AiClassificationResult.fallbackResult();
        }
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("No JSON found in AI response");
        }
        return content.substring(start, end + 1);
    }

    private String expectedUnit(SignalType type) {
        return switch (type) {
            case HAMBRE -> "Laboratorio de Nutricion";
            case ABANDONO -> "Unidad de Bienestar";
            case MUTACION -> "Division Genetica";
            case FUGA -> "Equipo de Contencion";
            case CONFLICTO -> "Consejo de Mediacion";
            case REPRODUCCION_MASIVA -> "Control Demografico";
            case SENAL_CORRUPTA -> "Archivo de Senales";
        };
    }
}
