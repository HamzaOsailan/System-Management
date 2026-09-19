
        package com.sahab.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sahab.demo.dto.AttendanceDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.service.AttendanceService;
import com.sahab.demo.service.WebAuthnService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final WebAuthnService webAuthnService;
    private final UserRepository userRepository;

    public AttendanceController(
            AttendanceService attendanceService,
            WebAuthnService webAuthnService,
            UserRepository userRepository
    ) {
        this.attendanceService = attendanceService;
        this.webAuthnService = webAuthnService;
        this.userRepository = userRepository;
    }

    @GetMapping("/today")
    public ResponseEntity<AttendanceDTO> today() {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                attendanceService.today(user)
        );
    }

    @GetMapping("/webauthn/status")
    public ResponseEntity<Boolean> status() {

        return ResponseEntity.ok(
                webAuthnService.isRegistered()
        );
    }

    @PostMapping("/webauthn/register/start")
    public ResponseEntity<?> startRegistration() {

        return ResponseEntity.ok(
                webAuthnService.startRegistration()
        );
    }

    @PostMapping("/webauthn/register/finish")
    public ResponseEntity<?> finishRegistration(
            @RequestBody JsonNode credentialJson
    ) {

        return ResponseEntity.ok(
                webAuthnService.finishRegistration(credentialJson)
        );
    }

    @PostMapping("/webauthn/authenticate/start")
    public ResponseEntity<?> startAuthentication() {

        return ResponseEntity.ok(
                webAuthnService.startAuthentication()
        );
    }

    @PostMapping("/webauthn/authenticate/finish")
    public ResponseEntity<?> finishAuthentication(
            @RequestBody JsonNode credentialJson
    ) {

        return ResponseEntity.ok(
                webAuthnService.finishAuthentication(credentialJson)
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }
}
