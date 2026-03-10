package com.sun.auth.security;

import com.sun.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JwtService — issues and validates JWT tokens.
 *
 * This service lives ONLY in auth-server.
 * The API Gateway uses the same jwt.secret to validate tokens independently
 * without calling this service on every request.
 *
 * Claims embedded in the token (forwarded by gateway as request headers):
 *   sub       → email          → X-Username
 *   userId    → user ID        → X-User-Id
 *   roles     → Set<String>   → X-User-Role
 */
@Slf4j
@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMs) {

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.signingKey         = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs       = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // ── Token Generation ──────────────────────────────────────────────────────

    /**
     * Generates an access token with user claims.
     * Downstream services read these claims from gateway-forwarded headers.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("roles",  user.getRoles());          // Set<String> e.g. ["USER", "ADMIN"]
        claims.put("fullName", user.getFullName());

        return buildToken(claims, user.getEmail(), expirationMs);
    }

    /**
     * Generates a refresh token — minimal claims, longer expiry.
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenId", UUID.randomUUID().toString()); // uniqueness per refresh

        return buildToken(claims, user.getEmail(), refreshExpirationMs);
    }

    // ── Token Validation ──────────────────────────────────────────────────────

    
    /**
     * Validates token against our User entity directly.
     * Accepts User (not UserDetails) to avoid type conflict — our User entity
     * always has email as username, so we compare against that directly.
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            return username.equals(user.getEmail()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claims Extraction ─────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private String buildToken(Map<String, Object> claims, String subject, long expiry) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(signingKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
