package com.example.security.controller;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import com.example.security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OAuth2 Controller
 * Handles OAuth2-specific endpoints
 */
@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    /**
     * Get OAuth2 user info
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getOAuth2User() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", oauth2User.getAttribute("name"));
            userInfo.put("email", oauth2User.getAttribute("email"));
            userInfo.put("picture", oauth2User.getAttribute("picture"));
            userInfo.put("provider", oauth2Token.getAuthorizedClientRegistrationId());
            
            return ResponseEntity.ok(userInfo);
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Not an OAuth2 user"));
    }

    /**
     * Link OAuth2 account with existing account
     */
    @PostMapping("/link")
    public ResponseEntity<Map<String, Object>> linkOAuth2Account(@RequestParam String email, @RequestParam String password) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (!(authentication instanceof OAuth2AuthenticationToken)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No OAuth2 authentication found",
                    "message", "Please authenticate with OAuth2 first"
                ));
            }
            
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            
            String oauth2Email = oauth2User.getAttribute("email");
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            String oauth2Id = oauth2User.getAttribute("sub"); // or "id" depending on provider
            String oauth2Name = oauth2User.getAttribute("name");
            String oauth2Picture = oauth2User.getAttribute("picture");
            
            // Verify that the OAuth2 email matches the provided email
            if (!oauth2Email.equals(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email mismatch",
                    "message", "OAuth2 email does not match the provided email"
                ));
            }
            
            // Use the service to handle the linking
            Map<String, Object> result = authService.linkOAuth2Account(
                email, password, provider, oauth2Id, oauth2Name, oauth2Picture
            );
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Linking failed",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error",
                "message", "An error occurred while linking the account"
            ));
        }
    }

    /**
     * Unlink OAuth2 account
     */
    @PostMapping("/unlink")
    public ResponseEntity<String> unlinkOAuth2Account() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            
            String email = oauth2User.getAttribute("email");
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            
            // Find and unlink the OAuth2 account
            userRepository.findByEmailAndOauth2Provider(email, provider)
                .ifPresent(user -> {
                    user.setOauth2Provider(null);
                    user.setOauth2Id(null);
                    userRepository.save(user);
                });
            
            return ResponseEntity.ok("OAuth2 account unlinked successfully");
        }
        
        return ResponseEntity.badRequest().body("Not an OAuth2 user");
    }
} 