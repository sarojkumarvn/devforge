package com.example.devforge.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.devforge.entity.User;
import com.example.devforge.entity.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class AuthUtil {

    // key
    @Value("${jwt.secretKey}")
    private String jwtsecretkey;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtsecretkey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getUserName())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSecretKey())
                .compact();

    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Optional<Long> getCurrentUserIdOptional() {
        return getCurrentUserOptional().map(User::getId);
    }

    public Long getCurrentCacheUserId() {
        return getCurrentUserIdOptional().orElse(0L);
    }

    public User getCurrentUser() {
        return getCurrentUserOptional()
                .orElseThrow(() -> new AccessDeniedException("Authenticated user is required"));
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User user)) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    public void requireCurrentUser(Long userId) {
        if (userId == null || !getCurrentUserId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to perform this action for another user");
        }
    }

    public boolean isAdmin() {
        return getCurrentUserOptional()
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public void requireCurrentUserOrAdmin(Long userId) {
        if (!isAdmin()) {
            requireCurrentUser(userId);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
