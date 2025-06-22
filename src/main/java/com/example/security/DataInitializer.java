package com.example.security;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Initializer Component
 * 
 * This component implements CommandLineRunner to initialize test users in the database when the application starts
 * 
 * Key Features:
 * - Implements CommandLineRunner for automatic execution on startup
 * - Creates test users with encoded passwords
 * - Uses @Component for Spring to detect and manage it
 * - Runs after all beans are initialized
 * 
 * Test Users Created:
 * - user/password (USER role)
 * - admin/admin123 (USER, ADMIN roles)
 * 
 * Security Notes:
 * - Passwords are encoded using BCryptPasswordEncoder
 * - Users are only created if they don't already exist
 * - This is for development/testing only - remove in production
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Initialize test data when the application starts.
     * 
     * This method is automatically called by Spring Boot after all beans are initialized.
     * It creates test users in the database for development and testing purposes.
     * 
     * @param args command line arguments (not used)
     * @throws Exception if any error occurs during initialization
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Initializing Test Data ===");

        // Create regular user
        createUserIfNotExists("user", "password", "user@example.com", Set.of("USER"));

        // Create admin user
        Set<String> adminRoles = new HashSet<>();
        adminRoles.add("USER");
        adminRoles.add("ADMIN");
        createUserIfNotExists("admin", "admin123", "admin@example.com", adminRoles);

        System.out.println("=== Test Data Initialization Complete ===");
        
        // Print all users for verification
        System.out.println("Users in database:");
        userRepository.findAll().forEach(user -> 
            System.out.println("- " + user.getUsername() + " (roles: " + user.getRoles() + ")")
        );
    }

    /**
     * Create a user if it doesn't already exist.
     * 
     * This method checks if a user with the given username exists, and if not,
     * creates a new user with the specified details.
     * 
     * @param username the username for the new user
     * @param password the plain text password (will be encoded)
     * @param email the email address for the user
     * @param roles the roles to assign to the user
     */
    private void createUserIfNotExists(String username, String password, String email, Set<String> roles) {
        if (!userRepository.existsByUsername(username)) {
            // Encode the password
            String encodedPassword = passwordEncoder.encode(password);
            
            // Create new user
            User user = new User(username, encodedPassword, email, roles);
            
            // Save to database
            User savedUser = userRepository.save(user);
            
            System.out.println("Created user: " + savedUser.getUsername() + " with roles: " + savedUser.getRoles());
        } else {
            System.out.println("User '" + username + "' already exists, skipping creation.");
        }
    }
} 