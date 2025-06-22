package com.example.security.service;

import com.example.security.config.JwtConfig;
import com.example.security.dto.AuthResponse;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.RegisterRequest;
import com.example.security.dto.TokenResponse;
import com.example.security.entity.User;
import com.example.security.jwt.JwtTokenUtil;
import com.example.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Register a new user and return tokens
     */
    public TokenResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);

        userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Map<String, String> tokens = jwtTokenUtil.generateTokenPair(userDetails);

        return new TokenResponse(
            tokens.get("accessToken"),
            tokens.get("refreshToken"),
            jwtConfig.getAccessTokenExpiration() / 1000
        );
    }

    /**
     * Authenticate user and return tokens
     */
    public TokenResponse login(LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
        );

        // Generate tokens
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, String> tokens = jwtTokenUtil.generateTokenPair(userDetails);

        return new TokenResponse(
            tokens.get("accessToken"),
            tokens.get("refreshToken"),
            jwtConfig.getAccessTokenExpiration() / 1000 // Convert to seconds
        );
    }

    /**
     * Refresh access token using refresh token
     */
    public TokenResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Extract username and load user details
        String username = jwtTokenUtil.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Generate new token pair
        Map<String, String> tokens = jwtTokenUtil.generateTokenPair(userDetails);

        return new TokenResponse(
            tokens.get("accessToken"),
            tokens.get("refreshToken"),
            jwtConfig.getAccessTokenExpiration() / 1000
        );
    }

    /**
     * Login a user with email
     */
    public AuthResponse loginWithEmail(String email, String password) {
        try {
            // First, find user by email
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return AuthResponse.error("User not found with this email");
            }
            
            User user = userOpt.get();
            
            // Use username for authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    password
                )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get the first role
            String role = user.getRoles().iterator().next();

            // Generate tokens
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
            Map<String, String> tokens = jwtTokenUtil.generateTokenPair(userDetails);

            return AuthResponse.success(
                "Login successful", 
                user.getUsername(), 
                role,
                tokens.get("accessToken")
            );

        } catch (Exception e) {
            return AuthResponse.error("Invalid email or password");
        }
    }

    /**
     * Get current user info
     */
    public AuthResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user is authenticated and not anonymous
        if (authentication == null || 
            !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            return AuthResponse.error("No user logged in");
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return AuthResponse.error("User not found");
        }

        User user = userOpt.get();
        String role = user.getRoles().iterator().next();

        // Generate tokens
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Map<String, String> tokens = jwtTokenUtil.generateTokenPair(userDetails);

        return AuthResponse.success(
            "Current user info", 
            user.getUsername(), 
            role,
            tokens.get("accessToken")
        );
    }
}