package com.sahab.demo.controller;

import com.sahab.demo.dto.EmployeeProfileDTO;
import com.sahab.demo.dto.UpdateEmployeeProfileDTO;
import com.sahab.demo.service.EmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee-profile")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;


    // =========================================================
    // GET MY PROFILE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileDTO> getMyProfile() {

        return ResponseEntity.ok(
                employeeProfileService.getCurrentProfile()
        );
    }


    // =========================================================
    // UPDATE MY PROFILE
    // =========================================================

    @PutMapping("/me")
    public ResponseEntity<EmployeeProfileDTO> updateMyProfile(
            @RequestBody UpdateEmployeeProfileDTO dto
    ) {

        return ResponseEntity.ok(
                employeeProfileService.updateCurrentProfile(dto)
        );
    }
}