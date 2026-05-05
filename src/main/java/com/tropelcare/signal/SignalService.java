package com.tropelcare.signal;

import com.tropelcare.ai.AiClassificationResult;
import com.tropelcare.ai.AiClient;
import com.tropelcare.care.CareResponse;
import com.tropelcare.care.CareResponseRepository;
import com.tropelcare.common.*;
import com.tropelcare.exception.BusinessException;
import com.tropelcare.exception.NotFoundException;
import com.tropelcare.guardian.Guardian;
import com.tropelcare.guardian.GuardianRepository;
import com.tropelcare.sector.Sector;
import com.tropelcare.sector.SectorRepository;
import com.tropelcare.tropel.Tropel;
import com.tropelcare.tropel.TropelRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SignalService {

    private final TropelSignalRepository signalRepository;
    private final TropelRepository tropelRepository;
    private final GuardianRepository guardianRepository;
    private final SectorRepository sectorRepository;
    private final CareResponseRepository careResponseRepository;
    private final AiClient aiClient;

    public SignalService(TropelSignalRepository signalRepository,
                         TropelRepository tropelRepository,
                         GuardianRepository guardianRepository,
                         SectorRepository sectorRepository,
                         CareResponseRepository careResponseRepository,
                         AiClient aiClient) {
        this.signalRepository = signalRepository;
        this.tropelRepository = tropelRepository;
        this.guardianRepository = guardianRepository;
        this.sectorRepository = sectorRepository;
        this.careResponseRepository = careResponseRepository;
        this.aiClient = aiClient;
    }

    @Transactional
    public SignalResponseDTO create(SignalRequestDTO request) {
        Tropel tropel = tropelRepository.findById(request.tropelId())
                .orElseThrow(() -> new NotFoundException("No existe un Tropel con id " + request.tropelId()));

        Guardian guardian = guardianRepository.findById(request.guardianId())
                .orElseThrow(() -> new NotFoundException("No existe un guardián con id " + request.guardianId()));

        if (!tropel.getGuardian().getId().equals(guardian.getId())) {
            throw new BusinessException("El guardianId no corresponde al guardián responsable de este Tropel");
        }

        AiClassificationResult classification = aiClient.classify(request.rawContent());

        if (!classification.fallback()) {
            applyTropelEffects(tropel, classification.severity());
            applySectorEffects(tropel.getSector(), classification.signalType());
        }

        Instant now = Instant.now();

        TropelSignal signal = new TropelSignal();
        signal.setTropel(tropel);
        signal.setGuardian(guardian);
        signal.setSenderTag(request.senderTag());
        signal.setRawContent(request.rawContent());
        signal.setSignalType(classification.signalType());
        signal.setSeverity(classification.severity());
        signal.setAssignedUnit(classification.assignedUnit());
        signal.setRecommendedAction(classification.recommendedAction());
        signal.setStatus(classification.fallback() ? SignalStatus.ERROR : SignalStatus.RECIBIDA);
        signal.setCreatedAt(now);
        signal.setUpdatedAt(now);

        TropelSignal saved = signalRepository.save(signal);

        CareResponse response = new CareResponse();
        response.setSignal(saved);
        response.setResponseCode(responseCodeFor(saved.getSignalType()));
        response.setDescription(saved.getRecommendedAction());
        response.setCreatedAt(now);
        careResponseRepository.save(response);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SignalResponseDTO findById(Long id) {
        TropelSignal signal = signalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe una señal con id " + id));
        return toResponse(signal);
    }

    @Transactional(readOnly = true)
    public PageResponse<SignalResponseDTO> findAll(SignalType signalType, Severity severity, SignalStatus status,
                                                   Long tropelId, Long guardianId, int page, int size) {
        Specification<TropelSignal> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (signalType != null) predicates.add(cb.equal(root.get("signalType"), signalType));
            if (severity != null) predicates.add(cb.equal(root.get("severity"), severity));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (tropelId != null) predicates.add(cb.equal(root.get("tropel").get("id"), tropelId));
            if (guardianId != null) predicates.add(cb.equal(root.get("guardian").get("id"), guardianId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SignalResponseDTO> result = signalRepository
                .findAll(specification, PageRequest.of(page, size))
                .map(this::toResponse);

        return PageResponse.from(result);
    }

    private void applyTropelEffects(Tropel tropel, Severity severity) {
        int energyDelta = switch (severity) {
            case LEVE -> -5;
            case MODERADO -> -10;
            case GRAVE -> -20;
            case CRITICO -> -30;
        };

        int chaosDelta = switch (severity) {
            case LEVE -> 5;
            case MODERADO -> 15;
            case GRAVE -> 30;
            case CRITICO -> 45;
        };

        int mutationDelta = severity == Severity.CRITICO ? 1 : 0;

        tropel.setEnergyLevel(clamp(tropel.getEnergyLevel() + energyDelta, 0, 100));
        tropel.setChaosIndex(clamp(tropel.getChaosIndex() + chaosDelta, 0, 100));
        tropel.setMutationStage(clamp(tropel.getMutationStage() + mutationDelta, 0, 5));

        if (tropel.getChaosIndex() >= 80) {
            tropel.setVitalState(VitalState.CRITICO);
        } else if (tropel.getEnergyLevel() <= 20) {
            tropel.setVitalState(VitalState.HAMBRIENTO);
        } else if (severity == Severity.CRITICO) {
            tropel.setVitalState(VitalState.MUTANDO);
        } else if (severity == Severity.GRAVE) {
            tropel.setVitalState(VitalState.AGITADO);
        }

        tropel.setUpdatedAt(Instant.now());
        tropelRepository.save(tropel);
    }

    private void applySectorEffects(Sector sector, SignalType signalType) {
        if (signalType == SignalType.FUGA) {
            sector.setStabilityLevel(Math.max(0, sector.getStabilityLevel() - 10));
            sectorRepository.save(sector);
        } else if (signalType == SignalType.REPRODUCCION_MASIVA) {
            sector.setStabilityLevel(Math.max(0, sector.getStabilityLevel() - 15));
            sectorRepository.save(sector);
        }
    }

    private String responseCodeFor(SignalType signalType) {
        return switch (signalType) {
            case HAMBRE -> "DISPATCH_NUTRIENT_PACK";
            case ABANDONO -> "SEND_COMPANIONSHIP_PROTOCOL";
            case MUTACION -> "ISOLATE_AND_OBSERVE";
            case FUGA -> "ACTIVATE_SECTOR_LOCK";
            case CONFLICTO -> "DEPLOY_MEDIATION_FIELD";
            case REPRODUCCION_MASIVA -> "ENABLE_POPULATION_CONTROL";
            case SENAL_CORRUPTA -> "ARCHIVE_AND_IGNORE";
        };
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private SignalResponseDTO toResponse(TropelSignal signal) {
        return new SignalResponseDTO(
                signal.getId(),
                signal.getTropel().getId(),
                signal.getTropel().getName(),
                signal.getGuardian().getId(),
                signal.getGuardian().getDisplayName(),
                signal.getSenderTag(),
                signal.getRawContent(),
                signal.getSignalType(),
                signal.getSeverity(),
                signal.getAssignedUnit(),
                signal.getRecommendedAction(),
                signal.getStatus(),
                signal.getCreatedAt(),
                signal.getUpdatedAt()
        );
    }
}
