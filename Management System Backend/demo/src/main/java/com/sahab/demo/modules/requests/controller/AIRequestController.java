package com.sahab.demo.modules.requests.controller;



import com.sahab.demo.modules.requests.dto.AIAnalysisDTO;
import com.sahab.demo.modules.requests.dto.AIUserAnalysisDTO;
import com.sahab.demo.modules.requests.service.AIRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
@CrossOrigin("*")
public class AIRequestController {

    private final AIRequestService aiRequestService;


    public AIRequestController(
            AIRequestService aiRequestService
    ) {
        this.aiRequestService = aiRequestService;
    }


    // =========================================================
    // ADMIN - GET AI ANALYSIS
    // =========================================================

    @GetMapping("/{id}/ai-analysis/admin")
    public ResponseEntity<AIAnalysisDTO> getAdminAnalysis(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                aiRequestService.getAdminAnalysis(id)
        );
    }


    // =========================================================
    // ADMIN - GENERATE AI ANALYSIS
    // =========================================================

    @PostMapping("/{id}/ai-analysis/generate")
    public ResponseEntity<AIAnalysisDTO> generateAIAnalysis(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                aiRequestService.generateAnalysisForAdmin(id)
        );
    }


    // =========================================================
    // USER - GET AI ANALYSIS
    // =========================================================

    @GetMapping("/{id}/ai-analysis")
    public ResponseEntity<AIUserAnalysisDTO> getUserAnalysis(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                aiRequestService.getUserAnalysis(id)
        );
    }
}

