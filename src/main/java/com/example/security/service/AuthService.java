package com.example.security.service;

import com.example.security.dto.AuthResponse;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.RegisterRequest;
import com.example.security.entity.User;
import com.example.security.jwt.JwtTokenUtil;
import com.example.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

/**
 * Service class for handling authentication and registration logic
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return AuthResponse.error("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return AuthResponse.error("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        // Set role (default to USER if not specified)
        String role = request.getRole() != null ? request.getRole() : "USER";
        user.setRoles(new HashSet<>(Arrays.asList(role)));

        // Save user to database
        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenUtil.generateToken(user.getUsername(), role);

        return AuthResponse.success(
            "User registered successfully", 
            user.getUsername(), 
            role,
            token
        );
    }

    /**
     * Login a user with JWT
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(), 
                    request.getPassword()
                )
            );

            // Get user details
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Get the first role (for simplicity)
            String role = user.getRoles().iterator().next();

            // Generate JWT token
            String token = jwtTokenUtil.generateToken(user.getUsername(), role);

            return AuthResponse.success(
                "Login successful", 
                user.getUsername(), 
                role,
                token
            );

        } catch (Exception e) {
            return AuthResponse.error("Invalid username or password");
        }
    }

    /**
     * Get current user info from JWT
     */
    public AuthResponse getCurrentUser(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token == null || !jwtTokenUtil.validateToken(token)) {
                return AuthResponse.error("Invalid or expired token");
            }

            String username = jwtTokenUtil.extractUsername(token);
            String role = jwtTokenUtil.extractRole(token);

            return AuthResponse.success(
                "Current user info", 
                username, 
                role,
                token
            );

        } catch (Exception e) {
            return AuthResponse.error("Error processing token");
        }
    }
} 