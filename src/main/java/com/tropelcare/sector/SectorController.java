package com.tropelcare.sector;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sectors")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectorResponseDTO create(@Valid @RequestBody SectorRequestDTO request) {
        return sectorService.create(request);
    }

    @GetMapping
    public List<SectorResponseDTO> findAll() {
        return sectorService.findAll();
    }

    @GetMapping("/{id}")
    public SectorResponseDTO findById(@PathVariable Long id) {
        return sectorService.findById(id);
    }
}
