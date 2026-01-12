package com.eduplatform.auth.rbac.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import io.jsonwebtoken.Claims;
import io.reactivex.rxjava3.core.Single;

import java.util.function.Function;

/**
 * TokenService interface - JWT operations với RxJava3 (Spring Boot 4 + Vert.x 5)
 * Cải tiến:
 * - Support refresh token
 * - Token blacklist
 * - Type-safe claim extraction
 */
public interface TokenService {

    /**
     * Generate access token
     */
    Single<String> generate(SecurityUser user);

    /**
     * Generate refresh token (longer expiration)
     */
    Single<String> generateRefreshToken(SecurityUser user);

    /**
     * Validate token signature and format
     */
    Single<Boolean> validate(String token);

    /**
     * Check if token is expired
     */
    Single<Boolean> isExpired(String token);

    /**
     * Extract claim using resolver function
     */
    <T> Single<T> getClaim(String token, Function<Claims, T> resolver);

    /**
     * Extract user ID from token
     */
    Single<Integer> extractUserId(String token);

    /**
     * Extract username from token
     */
    Single<String> extractUsername(String token);

    /**
     * Get full SecurityUser from token claims
     */
    Single<SecurityUser> getSecurityUser(String token);

    /**
     * Invalidate token (add to blacklist)
     */
    Single<Boolean> invalidate(String token);

    /**
     * Check if token is a refresh token
     */
    Single<Boolean> isRefreshToken(String token);
}
