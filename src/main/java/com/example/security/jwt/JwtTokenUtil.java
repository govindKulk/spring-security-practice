package com.example.security.jwt;

import com.example.security.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Utility Class
 * Handles JWT token generation, validation, and extraction
 */
@Component
public class JwtTokenUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * Generate a JWT token for a user
     * used while login , register
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate a JWT token with custom claims
     * used while login , register
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * Create JWT token with claims and subject
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
        /* 
         * 
         * equivalent to
         * return extractClaim(token, (Claims claims) -> claims.getSubject())
         */
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
        /* 
         * 
         * equivalent to
         * return extractClaim(token, (Claims claims) -> claims.getExpiration())
         */
    }

    /**
     * Extract a specific claim from JWT token
     * Function is a functional interface that takes a Claims object and returns a T object
     * Claims is a class that represents the claims of a JWT token
     * T is a generic type that represents the type of the claim to be extracted
     * although T is not specified, it is inferred from the function parameter, whihc is in this case Claims::getSubject or Claims:getExpiration
     * claimsResolver is a function that takes a Claims object and returns a T object


     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get the signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {

        // before is a method of Date class that checks if the date is before the given date
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate JWT token for a specific user
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // token is valid if the username is the same as the userDetails.getUsername() 
        // and the token is not expired
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validate JWT token (without UserDetails)
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract role from JWT token
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }
} 


/*
 * JWT Token Utility Class - Best Practice Order for Spring Security
 * 
 * The recommended order for implementing JWT utilities in Spring Security:
 * 
 * 1. CONFIGURATION & DEPENDENCIES
 *    - Constructor injection of JwtConfig
 *    - SecretKey generation method (getSigningKey)
 * 
 * 2. TOKEN GENERATION
 *    - generateToken(UserDetails)
 *    - generateToken(String username)
 *    - generateToken(String username, Map<String, Object> claims)
 * 
 * 3. CLAIM EXTRACTION (Core functionality)
 *    - extractAllClaims(String token) - Main extraction method
 *    - extractUsername(String token) - Most commonly used
 *    - extractExpiration(String token) - For validation
 *    - extractClaim(String token, Function<Claims, T> claimsResolver) - Generic extractor
 *    - extractRole(String token) - Business-specific claim
 * 
 * 4. TOKEN VALIDATION
 *    - isTokenExpired(String token) - Internal validation helper
 *    - validateToken(String token, UserDetails userDetails) - With user context
 *    - validateToken(String token) - Basic validation
 * 
 * 5. UTILITY METHODS
 *    - Helper methods for date handling
 *    - Error handling utilities
 * 
 * This order follows the principle of:
 * - Dependencies first (configuration)
 * - Core functionality (generation & extraction)
 * - Validation logic
 * - Utility methods last
 * 
 * Benefits of this order:
 * - Logical flow from creation to validation
 * - Easy to understand and maintain
 * - Follows Spring Security best practices
 * - Clear separation of concerns
 */