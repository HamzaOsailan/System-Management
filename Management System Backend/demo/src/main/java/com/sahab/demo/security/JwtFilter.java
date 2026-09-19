package com.sahab.demo.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ==============================
        // GET AUTH HEADER
        // ==============================

        final String authHeader =
                request.getHeader("Authorization");

        // إذا مافيه Bearer Token
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);

            return;
        }

        // ==============================
        // EXTRACT TOKEN
        // ==============================

        final String jwt =
                authHeader.substring(7);

        String userEmail = null;

        // ==============================
        // EXTRACT USERNAME SAFELY
        // ==============================

        try {

            userEmail =
                    jwtService.extractUsername(jwt);

        } catch (Exception e) {

            System.out.println("JWT ERROR: "
                    + e.getMessage());

            filterChain.doFilter(request, response);

            return;
        }

        // ==============================
        // CHECK SECURITY CONTEXT
        // ==============================

        if (userEmail != null &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(userEmail);

            // ==============================
            // VALIDATE TOKEN
            // ==============================

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =

                        new UsernamePasswordAuthenticationToken(

                                userDetails,

                                null,

                                userDetails.getAuthorities()
                        );

                authToken.setDetails(

                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // ==============================
                // SAVE AUTHENTICATION
                // ==============================

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}