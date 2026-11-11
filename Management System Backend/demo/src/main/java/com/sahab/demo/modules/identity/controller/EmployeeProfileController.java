package com.sahab.demo.modules.identity.controller;

import com.sahab.demo.modules.identity.dto.EmployeeProfileDTO;
import com.sahab.demo.modules.identity.dto.EmployeeProfileUpdateDTO;
import com.sahab.demo.modules.identity.service.EmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee-profile")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    // ==========================================
    // Get current logged-in user's profile
    // ==========================================

    @GetMapping
    public ResponseEntity<EmployeeProfileDTO> getMyProfile() {

        return ResponseEntity.ok(
                employeeProfileService.getMyProfile()
        );
    }

    // ==========================================
    // Update current logged-in user's profile
    // ==========================================

    @PutMapping
    public ResponseEntity<EmployeeProfileDTO> updateMyProfile(
            @RequestBody EmployeeProfileUpdateDTO dto
    ) {

        return ResponseEntity.ok(
                employeeProfileService.updateMyProfile(dto)
        );
    }

    // ==========================================
    // Change Password
    // ==========================================

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody EmployeeProfileService.ChangePasswordRequest request
    ) {

        employeeProfileService.changePassword(request);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "Password changed successfully"
                )
        );
    }
}