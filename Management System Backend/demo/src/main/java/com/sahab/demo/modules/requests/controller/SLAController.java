package com.sahab.demo.modules.requests.controller;

import com.sahab.demo.modules.requests.dto.SLADashboardResponse;
import com.sahab.demo.modules.requests.dto.SLAPredictionResponse;
import com.sahab.demo.entity.Request;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.modules.requests.service.SLAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sla")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SLAController {

    private final SLAService slaService;
    private final RequestRepository requestRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SLADashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                slaService.getDashboard()
        );
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SLAPredictionResponse> getRequestPrediction(
            @PathVariable Long id
    ) {

        Request request =
                requestRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found"
                                )
                        );

        return ResponseEntity.ok(
                slaService.predictRequest(request)
        );
    }
}