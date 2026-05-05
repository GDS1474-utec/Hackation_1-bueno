package com.tropelcare.signal;

import com.tropelcare.ai.AiClassificationResult;
import com.tropelcare.ai.AiClient;
import com.tropelcare.care.CareResponse;
import com.tropelcare.care.CareResponseRepository;
import com.tropelcare.common.*;
import com.tropelcare.exception.BusinessException;
import com.tropelcare.guardian.Guardian;
import com.tropelcare.guardian.GuardianRepository;
import com.tropelcare.sector.Sector;
import com.tropelcare.sector.SectorRepository;
import com.tropelcare.tropel.Tropel;
import com.tropelcare.tropel.TropelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SignalServiceTest {

    private TropelSignalRepository signalRepository;
    private TropelRepository tropelRepository;
    private GuardianRepository guardianRepository;
    private SectorRepository sectorRepository;
    private CareResponseRepository careResponseRepository;
    private AiClient aiClient;
    private ApplicationEventPublisher eventPublisher;
    private SignalService service;

    private Guardian guardian;
    private Sector sector;
    private Tropel tropel;

    @BeforeEach
    void setUp() {
        signalRepository = mock(TropelSignalRepository.class);
        tropelRepository = mock(TropelRepository.class);
        guardianRepository = mock(GuardianRepository.class);
        sectorRepository = mock(SectorRepository.class);
        careResponseRepository = mock(CareResponseRepository.class);
        aiClient = mock(AiClient.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new SignalService(
                signalRepository,
                tropelRepository,
                guardianRepository,
                sectorRepository,
                careResponseRepository,
                aiClient,
                eventPublisher
        );

        guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("team@gmail.com");
        guardian.setCreatedAt(Instant.now());

        sector = new Sector();
        sector.setId(1L);
        sector.setSectorCode("SECTOR-7");
        sector.setClimate(Climate.RETRO_ARCADE);
        sector.setCapacity(3);
        sector.setCurrentLoad(1);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        tropel = new Tropel();
        tropel.setId(1L);
        tropel.setName("BipBop");
        tropel.setSpecies(Species.GLITCHY);
        tropel.setVitalState(VitalState.ESTABLE);
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(Instant.now());
        tropel.setUpdatedAt(Instant.now());

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(signalRepository.save(any(TropelSignal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(careResponseRepository.save(any(CareResponse.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenAiReturnsValidJsonSignalIsPersistedWithCorrectClassificationAndStatusRecibida() {
        when(aiClient.classify(anyString())).thenReturn(new AiClassificationResult(
                SignalType.HAMBRE,
                Severity.MODERADO,
                "Laboratorio de Nutricion",
                "Enviar paquete de nutrientes.",
                false
        ));

        SignalResponseDTO response = service.create(validRequest());

        assertEquals(SignalType.HAMBRE, response.signalType());
        assertEquals(Severity.MODERADO, response.severity());
        assertEquals("Laboratorio de Nutricion", response.assignedUnit());
        assertEquals("Enviar paquete de nutrientes.", response.recommendedAction());
        assertEquals(SignalStatus.RECIBIDA, response.status());
        verify(signalRepository).save(any(TropelSignal.class));
        verify(careResponseRepository).save(any(CareResponse.class));
    }

    @Test
    void whenAiReturnsTextAroundJsonClientProvidesParsedResultAndServiceDoesNotThrow() {
        when(aiClient.classify(anyString())).thenReturn(new AiClassificationResult(
                SignalType.MUTACION,
                Severity.GRAVE,
                "Division Genetica",
                "Aislar y observar al Tropel.",
                false
        ));

        SignalResponseDTO response = assertDoesNotThrow(() -> service.create(validRequest()));

        assertEquals(SignalType.MUTACION, response.signalType());
        assertEquals(Severity.GRAVE, response.severity());
        assertEquals("Division Genetica", response.assignedUnit());
        assertEquals("Aislar y observar al Tropel.", response.recommendedAction());
    }

    @Test
    void whenAiFailsSignalIsPersistedWithFallbackAndExceptionIsNotPropagated() {
        when(aiClient.classify(anyString())).thenReturn(AiClassificationResult.fallbackResult());

        SignalResponseDTO response = assertDoesNotThrow(() -> service.create(validRequest()));

        assertEquals(SignalType.SENAL_CORRUPTA, response.signalType());
        assertEquals(Severity.LEVE, response.severity());
        assertEquals("Archivo de Senales", response.assignedUnit());
        assertEquals("Archivar la señal y revisar manualmente si se repite.", response.recommendedAction());
        assertEquals(SignalStatus.ERROR, response.status());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void whenSeverityIsCriticalChaosAndMutationIncreaseWithoutExceedingLimits() {
        tropel.setChaosIndex(70);
        tropel.setMutationStage(5);
        tropel.setEnergyLevel(80);

        when(aiClient.classify(anyString())).thenReturn(new AiClassificationResult(
                SignalType.MUTACION,
                Severity.CRITICO,
                "Division Genetica",
                "Aislar inmediatamente.",
                false
        ));

        service.create(validRequest());

        assertEquals(100, tropel.getChaosIndex());
        assertEquals(5, tropel.getMutationStage());
        assertEquals(50, tropel.getEnergyLevel());
        assertEquals(VitalState.CRITICO, tropel.getVitalState());
    }

    @Test
    void publishEventIsCalledOnceOnSuccessAndNeverOnFallback() {
        when(aiClient.classify(anyString())).thenReturn(new AiClassificationResult(
                SignalType.HAMBRE,
                Severity.LEVE,
                "Laboratorio de Nutricion",
                "Enviar nutrientes.",
                false
        ));

        service.create(validRequest());
        verify(eventPublisher, times(1)).publishEvent(any(TropelSignalCreatedEvent.class));

        reset(eventPublisher);
        when(aiClient.classify(anyString())).thenReturn(AiClassificationResult.fallbackResult());

        service.create(validRequest());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private SignalRequestDTO validRequest() {
        return new SignalRequestDTO(
                1L,
                1L,
                "sensor-genetico-1",
                "BipBop emite un brillo verde anómalo con cambios físicos visibles."
        );
    }
}
