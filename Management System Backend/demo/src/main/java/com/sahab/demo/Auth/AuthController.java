package com.sahab.demo.Auth;

import com.sahab.demo.dto.ForgotPasswordDTO;
import com.sahab.demo.dto.LoginRequest;
import com.sahab.demo.dto.ResetPasswordDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.security.JwtService;
import com.sahab.demo.service.EmailService;

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
@CrossOrigin(origins = "http://localhost:5173")
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

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
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
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email or password"
                    ));
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
                Map.of(
                        "token", token
                )
        );
    }


    // ===============================
    // جديد: طلب رابط استعادة الباسورد
    // ===============================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordDTO dto
    ) {

        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            String token = UUID.randomUUID().toString();

            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));

            userRepository.save(user);

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        }

        // نفس الرسالة سواء الإيميل موجود أو لا —
        // عشان ما نسرّب معلومة "هذا الإيميل مسجّل عندنا ولا لا"
        return ResponseEntity.ok(Map.of(
                "message",
                "If that email exists, a reset link has been sent"
        ));
    }


    // ===============================
    // جديد: تعيين باسورد جديد باستخدام التوكن
    // ===============================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordDTO dto
    ) {

        User user = userRepository.findByResetToken(dto.getToken())
                .orElse(null);

        boolean invalidOrExpired =
                user == null
                        || user.getResetTokenExpiry() == null
                        || user.getResetTokenExpiry().isBefore(LocalDateTime.now());

        if (invalidOrExpired) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Invalid or expired reset link"
                    ));
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Password must be at least 6 characters"
                    ));
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        // نلغي التوكن عشان ما ينستخدم مرة ثانية
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successful"
        ));
    }
}