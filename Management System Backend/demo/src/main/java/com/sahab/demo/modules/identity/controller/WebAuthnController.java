
package com.sahab.demo.modules.identity.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sahab.demo.modules.hr.dto.AttendanceDTO;
import com.sahab.demo.modules.identity.service.WebAuthnService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/attendance/webauthn")
@RequiredArgsConstructor
@CrossOrigin("*")
public class WebAuthnController {

    private final WebAuthnService webAuthnService;

    // =========================================================
    // CHECK BIOMETRIC REGISTRATION STATUS
    //
    // GET:
    // /attendance/webauthn/status
    //
    // USER + ADMIN + MANAGER + HR
    // =========================================================

    @GetMapping("/status")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<Map<String, Boolean>> status() {

        boolean registered =
                webAuthnService.isRegistered();

        return ResponseEntity.ok(
                Map.of(
                        "registered",
                        registered
                )
        );
    }

    // =========================================================
    // REGISTRATION START
    //
    // POST:
    // /attendance/webauthn/register/start
    //
    // USER + ADMIN + MANAGER + HR
    // =========================================================

    @PostMapping("/register/start")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<JsonNode> startRegistration() {

        JsonNode options =
                webAuthnService.startRegistration();

        return ResponseEntity.ok(options);
    }

    // =========================================================
    // REGISTRATION FINISH
    //
    // POST:
    // /attendance/webauthn/register/finish
    //
    // USER + ADMIN + MANAGER + HR
    // =========================================================

    @PostMapping("/register/finish")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<Map<String, String>> finishRegistration(
            @RequestBody JsonNode credential
    ) {

        String message =
                webAuthnService.finishRegistration(
                        credential
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        message
                )
        );
    }

    // =========================================================
    // AUTHENTICATION START
    //
    // POST:
    // /attendance/webauthn/authenticate/start
    //
    // USER + ADMIN + MANAGER + HR
    // =========================================================

    @PostMapping("/authenticate/start")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<JsonNode> startAuthentication() {

        JsonNode options =
                webAuthnService.startAuthentication();

        return ResponseEntity.ok(options);
    }

    // =========================================================
    // AUTHENTICATION FINISH
    //
    // POST:
    // /attendance/webauthn/authenticate/finish
    //
    // USER + ADMIN + MANAGER + HR
    // =========================================================

    @PostMapping("/authenticate/finish")
    @PreAuthorize(
            "hasAnyRole('USER', 'ADMIN', 'MANAGER', 'HR')"
    )
    public ResponseEntity<AttendanceDTO> finishAuthentication(
            @RequestBody JsonNode credential
    ) {

        AttendanceDTO attendance =
                webAuthnService.finishAuthentication(
                        credential
                );

        return ResponseEntity.ok(attendance);
    }
}

