package com.sahab.demo.modules.requests.controller;

import com.sahab.demo.modules.requests.dto.SlaPolicyDTO;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.modules.requests.service.SlaPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sla-policies")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SlaPolicyController {

    private final SlaPolicyService slaPolicyService;

    // =========================================================
    // GET ALL SLA POLICIES
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPolicies() {

        try {

            List<SlaPolicyDTO> policies =
                    slaPolicyService.getAllPolicies();

            return ResponseEntity.ok(
                    policies
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET SLA POLICY
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPolicy(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    slaPolicyService
                            .getPolicyById(id)
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // CREATE SLA POLICY
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPolicy(
            @RequestParam Long departmentId,
            @RequestParam Integer durationHours,
            @RequestParam Integer warningHours,
            @RequestParam(required = false) Boolean active
    ) {

        try {

            SlaPolicyDTO policy =
                    slaPolicyService.createPolicy(
                            departmentId,
                            durationHours,
                            warningHours,
                            active
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(policy);

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // UPDATE SLA POLICY
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePolicy(
            @PathVariable Long id,
            @RequestParam Long departmentId,
            @RequestParam Integer durationHours,
            @RequestParam Integer warningHours,
            @RequestParam(required = false) Boolean active
    ) {

        try {

            SlaPolicyDTO policy =
                    slaPolicyService.updatePolicy(
                            id,
                            departmentId,
                            durationHours,
                            warningHours,
                            active
                    );

            return ResponseEntity.ok(
                    policy
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePolicy(
            @PathVariable Long id
    ) {

        try {

            slaPolicyService.deletePolicy(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "SLA policy deleted successfully"
                    )
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}