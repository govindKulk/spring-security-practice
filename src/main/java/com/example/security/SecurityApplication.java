package com.example.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Class
 * 
 * This is the entry point of our Spring Security learning application.
 * Spring Boot will automatically configure Spring Security with default settings
 * when it detects the spring-boot-starter-security dependency.
 * 
 * Key points:
 * - @SpringBootApplication enables auto-configuration
 * - Spring Security will be automatically configured
 * - Default security rules will be applied
 * - We'll override these defaults in SecurityConfig
 */
@SpringBootApplication
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
        
        System.out.println("🚀 Spring Security Learning Application Started!");
        System.out.println("📍 Application URL: http://localhost:8080");
        System.out.println("🔧 H2 Console: http://localhost:8080/h2-console");
        System.out.println("📚 Test Endpoints:");
        System.out.println("   - Public: http://localhost:8080/public/hello");
        System.out.println("   - Private: http://localhost:8080/private/hello");
        System.out.println("   - Admin: http://localhost:8080/admin/hello");
    }
} 