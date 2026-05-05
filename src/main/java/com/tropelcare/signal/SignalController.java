package com.tropelcare.signal;

import com.tropelcare.common.PageResponse;
import com.tropelcare.common.Severity;
import com.tropelcare.common.SignalStatus;
import com.tropelcare.common.SignalType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalController {

    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignalResponseDTO create(@Valid @RequestBody SignalRequestDTO request) {
        return signalService.create(request);
    }

    @GetMapping("/{id}")
    public SignalResponseDTO findById(@PathVariable Long id) {
        return signalService.findById(id);
    }

    @GetMapping
    public PageResponse<SignalResponseDTO> findAll(
            @RequestParam(required = false) SignalType signalType,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) SignalStatus status,
            @RequestParam(required = false) Long tropelId,
            @RequestParam(required = false) Long guardianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return signalService.findAll(signalType, severity, status, tropelId, guardianId, page, size);
    }
}
