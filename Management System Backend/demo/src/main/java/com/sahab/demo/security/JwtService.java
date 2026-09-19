package com.sahab.demo.security;

import com.sahab.demo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET =
            "mysecretkeymysecretkeymysecretkey123456";

    private Key getSignInKey() {

        return Keys.hmacShaKeyFor(SECRET.getBytes());

    }

    // ==============================
    // GENERATE TOKEN
    // ==============================

    public String generateToken(User user) {

        return Jwts.builder()

                .setSubject(user.getEmail())

                .claim("role", user.getRole().name())

                .setIssuedAt(new Date())

                .setExpiration(
                        new Date(System.currentTimeMillis() + 1000 * 60 * 60)
                )

                .signWith(getSignInKey(), SignatureAlgorithm.HS256)

                .compact();
    }

    // ==============================
    // EXTRACT USERNAME
    // ==============================

    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);

    }

    // ==============================
    // EXTRACT ROLE
    // ==============================

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    // ==============================
    // VALIDATE TOKEN
    // ==============================

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // ==============================
    // CHECK EXPIRATION
    // ==============================

    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());

    }

    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);

    }

    // ==============================
    // EXTRACT CLAIM
    // ==============================

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);

    }

    // ==============================
    // EXTRACT ALL CLAIMS
    // ==============================

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()

                .setSigningKey(getSignInKey())

                .build()

                .parseClaimsJws(token)

                .getBody();
    }
}