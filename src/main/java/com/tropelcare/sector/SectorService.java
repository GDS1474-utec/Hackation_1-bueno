package com.tropelcare.sector;

import com.tropelcare.exception.DuplicateResourceException;
import com.tropelcare.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;

    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    @Transactional
    public SectorResponseDTO create(SectorRequestDTO request) {
        if (sectorRepository.existsBySectorCode(request.sectorCode())) {
            throw new DuplicateResourceException("Ya existe un sector con código " + request.sectorCode());
        }

        Sector sector = new Sector();
        sector.setSectorCode(request.sectorCode());
        sector.setClimate(request.climate());
        sector.setCapacity(request.capacity());
        sector.setCurrentLoad(0);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        return toResponse(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public List<SectorResponseDTO> findAll() {
        return sectorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SectorResponseDTO findById(Long id) {
        Sector sector = sectorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe un sector con id " + id));
        return toResponse(sector);
    }

    private SectorResponseDTO toResponse(Sector sector) {
        return new SectorResponseDTO(
                sector.getId(),
                sector.getSectorCode(),
                sector.getClimate(),
                sector.getCapacity(),
                sector.getCurrentLoad(),
                sector.getStabilityLevel(),
                sector.getCreatedAt()
        );
    }
}
