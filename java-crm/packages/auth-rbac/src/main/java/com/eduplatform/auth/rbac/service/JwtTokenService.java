package com.eduplatform.auth.rbac.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * JwtTokenService - JWT Token Service với Spring Boot 4 + Vert.x 5
 * Cải tiến: Thread-safe, Blacklist support, Refresh token
 */
@Service
public class JwtTokenService implements TokenService {

    @Value("${jwt.secret:dGhpcyBpcyBhIHZlcnkgc2VjdXJlIGtleSBmb3IgZGV2ZWxvcG1lbnQgb25seQ==}")
    private String secretKey;

    @Value("${jwt.access-token.expiration:3600000}")
    private long accessTokenExpiration; // 1 hour

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration; // 7 days

    // Token blacklist (production: use Redis)
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public Single<String> generate(SecurityUser user) {
        return Single.fromCallable(() -> buildToken(user, accessTokenExpiration))
                .subscribeOn(Schedulers.computation());
    }

    /**
     * Generate refresh token với expiration dài hơn
     */
    public Single<String> generateRefreshToken(SecurityUser user) {
        return Single.fromCallable(() -> buildToken(user, refreshTokenExpiration))
                .subscribeOn(Schedulers.computation());
    }

    private String buildToken(SecurityUser user, long expiration) {
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMillis(expiration));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim("id", user.getId() != null ? user.getId().toString() : null)
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", user.getRoleCodes())
                .claim("type", expiration == refreshTokenExpiration ? "refresh" : "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public Single<Boolean> validate(String token) {
        return Single.fromCallable(() -> {
            if (isBlacklisted(token)) {
                return false;
            }
            try {
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }).subscribeOn(Schedulers.computation());
    }

    @Override
    public Single<Boolean> isExpired(String token) {
        if (isBlacklisted(token)) {
            return Single.just(true);
        }
        return getClaim(token, Claims::getExpiration)
                .map(exp -> exp.toInstant().isBefore(Instant.now()))
                .onErrorReturnItem(true);
    }

    @Override
    public <T> Single<T> getClaim(String token, Function<Claims, T> resolver) {
        return Single.fromCallable(() -> {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return resolver.apply(claims);
        }).subscribeOn(Schedulers.computation());
    }

    @Override
    public Single<Integer> extractUserId(String token) {
        return getClaim(token, claims -> {
            Object id = claims.get("id");
            if (id instanceof String) {
                // UUID stored as string
                return null; // Use extractUserUUID instead
            }
            return (Integer) id;
        });
    }

    public Single<UUID> extractUserUUID(String token) {
        return getClaim(token, claims -> {
            Object id = claims.get("id");
            if (id instanceof String) {
                return UUID.fromString((String) id);
            }
            return (UUID) id;
        });
    }

    @Override
    public Single<String> extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Single<SecurityUser> getSecurityUser(String token) {
        return getClaim(token, claims -> {
            Object idObj = claims.get("id");
            UUID userId;
            if (idObj instanceof String) {
                userId = UUID.fromString((String) idObj);
            } else {
                userId = (UUID) idObj;
            }
            
            return SecurityUser.builder()
                    .id(userId)
                    .username(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .name(claims.get("name", String.class))
                    .roleCodes((List<String>) claims.get("roles", List.class))
                    .build();
        });
    }

    @Override
    public Single<Boolean> invalidate(String token) {
        return Single.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                
                // Add to blacklist until expiration
                blacklistedTokens.put(token, claims.getExpiration().toInstant());
                cleanupBlacklist();
                return true;
            } catch (Exception e) {
                return false;
            }
        }).subscribeOn(Schedulers.computation());
    }

    /**
     * Check if token is refresh token
     */
    public Single<Boolean> isRefreshToken(String token) {
        return getClaim(token, claims -> "refresh".equals(claims.get("type", String.class)))
                .onErrorReturnItem(false);
    }

    private boolean isBlacklisted(String token) {
        Instant expiry = blacklistedTokens.get(token);
        if (expiry == null) return false;
        if (expiry.isBefore(Instant.now())) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void cleanupBlacklist() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
