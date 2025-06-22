package com.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for Spring Security Learning
 * 
 * This controller provides endpoints at different security levels to demonstrate
 * how Spring Security configuration works:
 * 
 * - /public/** - No authentication required
 * - /private/** - Requires authentication (any role)
 * - /admin/** - Requires ADMIN role
 * 
 * Each endpoint returns information about the current user and security context.
 */
@RestController
public class TestController {

    /**
     * Public endpoint - no authentication required
     * 
     * This endpoint can be accessed by anyone without logging in.
     * It demonstrates the permitAll() security rule.
     */
    @GetMapping("/public/hello")
    public Map<String, Object> publicHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from PUBLIC endpoint!");
        response.put("timestamp", LocalDateTime.now());
        response.put("securityLevel", "PUBLIC");
        response.put("description", "This endpoint requires no authentication");
        
        // Check if user is authenticated (optional)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            response.put("currentUser", auth.getName());
            response.put("userRoles", auth.getAuthorities());
        } else {
            response.put("currentUser", "Anonymous");
            response.put("userRoles", "None");
        }
        
        return response;
    }

    /**
     * Private endpoint - requires authentication
     * 
     * This endpoint requires the user to be logged in with any role.
     * It demonstrates the authenticated() security rule.
     */
    @GetMapping("/private/hello")
    public Map<String, Object> privateHello() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from PRIVATE endpoint!");
        response.put("timestamp", LocalDateTime.now());
        response.put("securityLevel", "PRIVATE");
        response.put("description", "This endpoint requires authentication");
        response.put("currentUser", auth.getName());
        response.put("userRoles", auth.getAuthorities());
        response.put("isAuthenticated", auth.isAuthenticated());
        
        return response;
    }

    /**
     * Admin endpoint - requires ADMIN role
     * 
     * This endpoint requires the user to have the ADMIN role.
     * It demonstrates the hasRole("ADMIN") security rule.
     */
    @GetMapping("/admin/hello")
    public Map<String, Object> adminHello() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from ADMIN endpoint!");
        response.put("timestamp", LocalDateTime.now());
        response.put("securityLevel", "ADMIN");
        response.put("description", "This endpoint requires ADMIN role");
        response.put("currentUser", auth.getName());
        response.put("userRoles", auth.getAuthorities());
        response.put("isAuthenticated", auth.isAuthenticated());
        
        return response;
    }

    /**
     * Root endpoint - requires authentication
     * 
     * This endpoint demonstrates the anyRequest().authenticated() rule.
     * Any URL not matching the specific patterns will require authentication.
     */
    @GetMapping("/")
    public Map<String, Object> home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Spring Security Learning!");
        response.put("timestamp", LocalDateTime.now());
        response.put("description", "This is the home page - requires authentication");
        response.put("currentUser", auth.getName());
        response.put("userRoles", auth.getAuthorities());
        response.put("availableEndpoints", Map.of(
            "public", "/public/hello",
            "private", "/private/hello", 
            "admin", "/admin/hello"
        ));
        
        return response;
    }

    /**
     * User info endpoint - requires authentication
     * 
     * This endpoint provides detailed information about the current user.
     */
    @GetMapping("/user/info")
    public Map<String, Object> userInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("isAuthenticated", auth.isAuthenticated());
        response.put("principal", auth.getPrincipal().getClass().getSimpleName());
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }
} 