package com.tropelcare.tropel;

import com.tropelcare.common.PageResponse;
import com.tropelcare.common.Species;
import com.tropelcare.common.VitalState;
import com.tropelcare.exception.BusinessException;
import com.tropelcare.exception.DuplicateResourceException;
import com.tropelcare.exception.NotFoundException;
import com.tropelcare.guardian.Guardian;
import com.tropelcare.guardian.GuardianRepository;
import com.tropelcare.sector.Sector;
import com.tropelcare.sector.SectorRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class TropelService {

    private final TropelRepository tropelRepository;
    private final SectorRepository sectorRepository;
    private final GuardianRepository guardianRepository;

    public TropelService(
            TropelRepository tropelRepository,
            SectorRepository sectorRepository,
            GuardianRepository guardianRepository
    ) {
        this.tropelRepository = tropelRepository;
        this.sectorRepository = sectorRepository;
        this.guardianRepository = guardianRepository;
    }

    @Transactional
    public TropelResponseDTO create(TropelRequestDTO request) {
        Sector sector = sectorRepository.findById(request.sectorId())
                .orElseThrow(() -> new NotFoundException("No existe un sector con id " + request.sectorId()));

        Guardian guardian = guardianRepository.findById(request.guardianId())
                .orElseThrow(() -> new NotFoundException("No existe un guardián con id " + request.guardianId()));

        if (tropelRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Ya existe un Tropel con nombre " + request.name());
        }

        if (sector.getCurrentLoad() >= sector.getCapacity()) {
            throw new BusinessException("El sector está lleno");
        }

        Instant now = Instant.now();

        Tropel tropel = new Tropel();
        tropel.setName(request.name());
        tropel.setSpecies(request.species());
        tropel.setVitalState(VitalState.ESTABLE);
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(now);
        tropel.setUpdatedAt(now);

        sector.setCurrentLoad(sector.getCurrentLoad() + 1);
        sectorRepository.save(sector);

        Tropel saved = tropelRepository.save(tropel);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TropelResponseDTO findById(Long id) {
        Tropel tropel = tropelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe un Tropel con id " + id));
        return toResponse(tropel);
    }

    @Transactional(readOnly = true)
    public PageResponse<TropelResponseDTO> findAll(
            Species species,
            VitalState vitalState,
            Long sectorId,
            Long guardianId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Tropel> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (species != null) {
                predicates.add(cb.equal(root.get("species"), species));
            }

            if (vitalState != null) {
                predicates.add(cb.equal(root.get("vitalState"), vitalState));
            }

            if (sectorId != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), sectorId));
            }

            if (guardianId != null) {
                predicates.add(cb.equal(root.get("guardian").get("id"), guardianId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TropelResponseDTO> result = tropelRepository.findAll(specification, pageable)
                .map(this::toResponse);

        return PageResponse.from(result);
    }

    private TropelResponseDTO toResponse(Tropel tropel) {
        return new TropelResponseDTO(
                tropel.getId(),
                tropel.getName(),
                tropel.getSpecies(),
                tropel.getVitalState(),
                tropel.getEnergyLevel(),
                tropel.getChaosIndex(),
                tropel.getMutationStage(),
                tropel.getSector().getId(),
                tropel.getSector().getSectorCode(),
                tropel.getGuardian().getId(),
                tropel.getGuardian().getDisplayName(),
                tropel.getCreatedAt(),
                tropel.getUpdatedAt()
        );
    }
}
