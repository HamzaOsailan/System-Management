package com.sahab.demo.controller;

import com.sahab.demo.dto.ChangePasswordDTO;
import com.sahab.demo.dto.UpdateProfileDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// ملاحظة: ما نحطه تحت /auth، لأن JwtAuthenticationFilter
// يتجاوز فحص التوكن بالكامل لأي مسار يبدأ بـ /auth.
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MeController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getCurrentUser() {

        User user = getCurrentUserEntity();

        return ResponseEntity.ok(Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }


    // ===============================
    // جديد: تعديل الاسم
    // ===============================
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileDTO dto
    ) {

        if (dto.getName() == null || dto.getName().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Name cannot be empty"));
        }

        User user = getCurrentUserEntity();

        user.setName(dto.getName().trim());

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }


    // ===============================
    // جديد: تغيير كلمة المرور
    // ===============================
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordDTO dto
    ) {

        User user = getCurrentUserEntity();

        boolean currentPasswordMatches = passwordEncoder.matches(
                dto.getCurrentPassword(),
                user.getPassword()
        );

        if (!currentPasswordMatches) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Current password is incorrect"
                    ));
        }

        if (dto.getNewPassword() == null
                || dto.getNewPassword().length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "New password must be at least 6 characters"
                    ));
        }

        user.setPassword(
                passwordEncoder.encode(dto.getNewPassword())
        );

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Password updated")
        );
    }


    // ===============================
    // دالة مشتركة: تجيب المستخدم الحالي من التوكن
    // ===============================
    private User getCurrentUserEntity() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

}