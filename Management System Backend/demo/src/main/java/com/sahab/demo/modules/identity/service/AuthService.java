package com.sahab.demo.modules.identity.service;
import com.sahab.demo.modules.identity.dto.AuthResponse;
import com.sahab.demo.modules.identity.dto.RegisterRequest;


import com.sahab.demo.dto.LoginRequest;
import com.sahab.demo.dto.ResetPasswordDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.security.JwtService;
import com.sahab.demo.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);

    /*
     * Production password policy.
     *
     * Existing passwords are NOT affected.
     * This applies when registering or changing a password.
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /*
     * Password reset token validity.
     */
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ==========================================
    // REGISTER
    // ==========================================

    @Transactional
    public AuthResponse register(
            RegisterRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Registration data is required"
            );
        }

        if (
                request.getEmail() == null ||
                        request.getEmail().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        if (
                request.getPassword() == null ||
                        request.getPassword().length()
                                < MIN_PASSWORD_LENGTH
        ) {

            throw new IllegalArgumentException(
                    "Password must be at least "
                            + MIN_PASSWORD_LENGTH
                            + " characters"
            );
        }

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        /*
         * Check whether the email already exists.
         */
        if (
                userRepository
                        .findByEmail(email)
                        .isPresent()
        ) {

            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        User user = new User();

        user.setName(
                request.getName()
        );

        user.setEmail(
                email
        );

        /*
         * Never store a raw password.
         */
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        /*
         * Public registration always creates
         * a normal USER.
         *
         * A client cannot register itself as:
         * ADMIN
         * MANAGER
         * HR
         */
        user.setRole(
                Role.USER
        );

        userRepository.save(user);

        /*
         * Generate JWT after the user has been saved.
         */
        String token =
                jwtService.generateToken(user);

        logger.info(
                "User registration successful"
        );

        return new AuthResponse(token);
    }

    // ==========================================
    // LOGIN
    // ==========================================

    public AuthResponse login(
            LoginRequest request
    ) {

        if (
                request == null ||
                        request.getEmail() == null ||
                        request.getPassword() == null
        ) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        /*
         * Do not reveal whether the email exists.
         *
         * This prevents basic user enumeration.
         */
        if (user == null) {

            logger.warn(
                    "Login failed: invalid credentials"
            );

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        /*
         * Defensive check in case an old/broken account
         * has no password.
         */
        if (user.getPassword() == null) {

            logger.error(
                    "Login failed: account has no password configured"
            );

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            logger.warn(
                    "Login failed: invalid credentials"
            );

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        /*
         * Generate JWT only after successful
         * authentication.
         */
        String token =
                jwtService.generateToken(user);

        logger.info(
                "User login successful"
        );

        return new AuthResponse(token);
    }

    // ==========================================
    // CREATE PASSWORD RESET TOKEN
    // ==========================================

    @Transactional
    public void createPasswordResetToken(
            String email,
            String frontendUrl,
            EmailService emailService
    ) {

        /*
         * Do not reveal whether an account exists.
         *
         * The controller can always return the same
         * response to the frontend.
         */
        if (
                email == null ||
                        email.isBlank()
        ) {

            return;
        }

        String normalizedEmail =
                normalizeEmail(email);

        userRepository
                .findByEmail(normalizedEmail)
                .ifPresent(user -> {

                    /*
                     * UUID provides a cryptographically random
                     * reset token suitable for this purpose.
                     */
                    String token =
                            UUID.randomUUID().toString();

                    user.setResetToken(
                            token
                    );

                    user.setResetTokenExpiry(
                            LocalDateTime
                                    .now()
                                    .plusMinutes(
                                            RESET_TOKEN_EXPIRY_MINUTES
                                    )
                    );

                    userRepository.save(user);

                    /*
                     * Build reset link.
                     *
                     * frontendUrl should come from a trusted
                     * server-side configuration, not an arbitrary
                     * user-controlled URL.
                     */
                    String resetLink =
                            frontendUrl
                                    + "/reset-password?token="
                                    + token;

                    emailService
                            .sendPasswordResetEmail(
                                    user.getEmail(),
                                    resetLink
                            );

                    logger.info(
                            "Password reset email requested"
                    );
                });
    }

    // ==========================================
    // RESET PASSWORD
    // ==========================================

    @Transactional
    public void resetPassword(
            ResetPasswordDTO dto
    ) {

        if (
                dto == null ||
                        dto.getToken() == null ||
                        dto.getToken().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset link"
            );
        }

        if (
                dto.getNewPassword() == null ||
                        dto.getNewPassword().length()
                                < MIN_PASSWORD_LENGTH
        ) {

            throw new IllegalArgumentException(
                    "Password must be at least "
                            + MIN_PASSWORD_LENGTH
                            + " characters"
            );
        }

        User user =
                userRepository
                        .findByResetToken(
                                dto.getToken()
                        )
                        .orElse(null);

        boolean invalidOrExpired =
                user == null
                        ||
                        user.getResetTokenExpiry() == null
                        ||
                        user.getResetTokenExpiry()
                                .isBefore(
                                        LocalDateTime.now()
                                );

        if (invalidOrExpired) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset link"
            );
        }

        /*
         * Never store the new password directly.
         */
        user.setPassword(
                passwordEncoder.encode(
                        dto.getNewPassword()
                )
        );

        /*
         * Token becomes invalid immediately after
         * successful password reset.
         */
        user.setResetToken(null);

        user.setResetTokenExpiry(null);

        userRepository.save(user);

        logger.info(
                "Password reset completed successfully"
        );
    }

    // ==========================================
    // NORMALIZE EMAIL
    // ==========================================

    private String normalizeEmail(
            String email
    ) {

        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }
}