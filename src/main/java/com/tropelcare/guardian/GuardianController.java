package com.tropelcare.guardian;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guardians")
public class GuardianController {

    private final GuardianService guardianService;

    public GuardianController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @GetMapping
    public List<GuardianResponseDTO> findAll() {
        return guardianService.findAll();
    }

    @GetMapping("/{id}")
    public GuardianResponseDTO findById(@PathVariable Long id) {
        return guardianService.findById(id);
    }
}
