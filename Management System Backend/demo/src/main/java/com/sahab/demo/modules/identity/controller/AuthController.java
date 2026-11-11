
package com.sahab.demo.modules.identity.controller;

import com.sahab.demo.dto.ForgotPasswordDTO;
import com.sahab.demo.dto.LoginRequest;
import com.sahab.demo.dto.ResetPasswordDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.security.JwtService;
import com.sahab.demo.service.EmailService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthController(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            log.warn("Login attempt rejected: email is missing");

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Email is required"
                    ));
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            log.warn("Login attempt rejected: password is missing");

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Password is required"
                    ));
        }

        String email = request.getEmail().trim();

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {

            /*
             * Do not log the submitted email here.
             * Authentication failures should not expose
             * user-identifying information in application logs.
             */
            log.warn("Login failed: invalid credentials");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email or password"
                    ));
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            log.warn("Login failed: invalid credentials");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email or password"
                    ));
        }

        String token =
                jwtService.generateToken(user);

        log.info("User authentication successful");

        return ResponseEntity.ok(
                Map.of(
                        "token", token
                )
        );
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordDTO dto
    ) {

        Optional<User> userOpt =
                userRepository.findByEmail(
                        dto.getEmail()
                );

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            String token =
                    UUID.randomUUID().toString();

            user.setResetToken(token);

            user.setResetTokenExpiry(
                    LocalDateTime.now().plusMinutes(30)
            );

            userRepository.save(user);

            String resetLink =
                    frontendUrl
                            + "/reset-password?token="
                            + token;

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    resetLink
            );

            log.info("Password reset request processed");
        }

        /*
         * Keep the response identical whether the email exists
         * or not to prevent user/account enumeration.
         */
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If that email exists, a reset link has been sent"
                )
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordDTO dto
    ) {

        User user =
                userRepository
                        .findByResetToken(dto.getToken())
                        .orElse(null);

        boolean invalidOrExpired =
                user == null
                        || user.getResetTokenExpiry() == null
                        || user.getResetTokenExpiry()
                        .isBefore(LocalDateTime.now());

        if (invalidOrExpired) {

            log.warn("Password reset rejected: invalid or expired token");

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid or expired reset link"
                            )
                    );
        }

        if (dto.getNewPassword() == null ||
                dto.getNewPassword().length() < 6) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password must be at least 6 characters"
                            )
                    );
        }

        user.setPassword(
                passwordEncoder.encode(
                        dto.getNewPassword()
                )
        );

        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        log.info("Password reset completed successfully");

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successful"
                )
        );
    }
}

