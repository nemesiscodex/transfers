package org.nemesiscodex.transfers.core.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationHours;

    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration-hours}") long expirationHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    // Security: generate token with user claims (id, username) and expiration
    public String generateToken(UUID userId, String username) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
            .subject(username)
            .claim("userId", userId.toString())
            .claim("username", username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
    }

    // Security: extract username from token subject claim
    public Mono<String> extractUsername(String token) {
        try {
            Claims claims = extractClaims(token);
            return Mono.just(claims.getSubject());
        } catch (Exception ex) {
            return Mono.error(new IllegalArgumentException("Invalid token", ex));
        }
    }

    // Security: validate token signature and expiration
    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = extractClaims(token);
            String tokenUsername = claims.getSubject();
            return username.equals(tokenUsername) && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    // Security: extract all claims after verifying signature
    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // Security: check if token expiration time has passed
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }
}

