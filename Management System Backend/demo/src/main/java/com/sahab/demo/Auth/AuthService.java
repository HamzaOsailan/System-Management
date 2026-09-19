package com.sahab.demo.Auth;

import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import com.sahab.demo.repository.UserRepository;
import com.sahab.demo.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    // =====================================
    // REGISTER
    // =====================================

    public AuthResponse register(RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);

        userRepository.save(user);

        // إنشاء JWT TOKEN
        String token =
                jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    // =====================================
    // LOGIN
    // =====================================

    public AuthResponse login(LoginRequest request) {

        User user = userRepository

                .findByEmail(request.getEmail())

                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // التحقق من كلمة المرور
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException("Invalid password");
        }

        // إنشاء TOKEN
        String token =
                jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}