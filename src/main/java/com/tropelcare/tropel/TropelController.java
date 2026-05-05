package com.tropelcare.tropel;

import com.tropelcare.common.PageResponse;
import com.tropelcare.common.Species;
import com.tropelcare.common.VitalState;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tropels")
public class TropelController {

    private final TropelService tropelService;

    public TropelController(TropelService tropelService) {
        this.tropelService = tropelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TropelResponseDTO create(@Valid @RequestBody TropelRequestDTO request) {
        return tropelService.create(request);
    }

    @GetMapping("/{id}")
    public TropelResponseDTO findById(@PathVariable Long id) {
        return tropelService.findById(id);
    }

    @GetMapping
    public PageResponse<TropelResponseDTO> findAll(
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) VitalState vitalState,
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) Long guardianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return tropelService.findAll(species, vitalState, sectorId, guardianId, page, size);
    }
}
