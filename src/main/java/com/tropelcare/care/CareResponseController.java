package com.tropelcare.care;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signals")
public class CareResponseController {

    private final CareResponseService careResponseService;

    public CareResponseController(CareResponseService careResponseService) {
        this.careResponseService = careResponseService;
    }

    @GetMapping("/{id}/care-response")
    public CareResponseDTO findBySignalId(@PathVariable Long id) {
        return careResponseService.findBySignalId(id);
    }
}
