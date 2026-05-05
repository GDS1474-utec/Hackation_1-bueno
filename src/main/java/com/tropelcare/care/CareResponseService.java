package com.tropelcare.care;

import com.tropelcare.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareResponseService {

    private final CareResponseRepository careResponseRepository;

    public CareResponseService(CareResponseRepository careResponseRepository) {
        this.careResponseRepository = careResponseRepository;
    }

    @Transactional(readOnly = true)
    public CareResponseDTO findBySignalId(Long signalId) {
        CareResponse response = careResponseRepository.findBySignalId(signalId)
                .orElseThrow(() -> new NotFoundException("No existe una respuesta de cuidado para la señal #" + signalId));

        return new CareResponseDTO(
                response.getId(),
                response.getSignal().getId(),
                response.getResponseCode(),
                response.getDescription(),
                response.getCreatedAt()
        );
    }
}
