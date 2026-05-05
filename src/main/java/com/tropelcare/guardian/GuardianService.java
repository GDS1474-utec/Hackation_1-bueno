package com.tropelcare.guardian;

import com.tropelcare.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuardianService {

    private final GuardianRepository guardianRepository;

    public GuardianService(GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    @Transactional(readOnly = true)
    public List<GuardianResponseDTO> findAll() {
        return guardianRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuardianResponseDTO findById(Long id) {
        Guardian guardian = guardianRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe un guardián con id " + id));
        return toResponse(guardian);
    }

    private GuardianResponseDTO toResponse(Guardian guardian) {
        return new GuardianResponseDTO(
                guardian.getId(),
                guardian.getDisplayName(),
                guardian.getEmail(),
                guardian.getNotificationEmail(),
                guardian.getCreatedAt()
        );
    }
}
