package com.example.security.oauth2;

import com.example.security.dto.TokenResponse;
import com.example.security.entity.User;
import com.example.security.jwt.JwtTokenUtil;
import com.example.security.repository.UserRepository;
import com.example.security.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 Success Handler
 * Handles successful OAuth2 authentication and generates JWT tokens
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) 
            throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            
            // Extract OAuth2 user information
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            String oauth2Id = oauth2User.getName();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String pictureUrl = oauth2User.getAttribute("picture");

            System.out.println("provider: " + provider);
            // Find or create user
            User user = findOrCreateOAuth2User(provider, oauth2Id, email, name, pictureUrl);

            // Generate JWT tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            Map<String, String> tokens = jwtTokenUtil.generateTokenPair(userDetails);

            // Create response
            TokenResponse tokenResponse = new TokenResponse(
                tokens.get("accessToken"),
                tokens.get("refreshToken"),
                900 // 15 minutes in seconds
            );

            // Set response headers
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Write JSON response
            String jsonResponse = objectMapper.writeValueAsString(tokenResponse);
            response.getWriter().write(jsonResponse);

            // Redirect to frontend with tokens (alternative approach)
            // String redirectUrl = "http://localhost:3000/oauth-success?access_token=" + tokens.get("accessToken");
            // getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    /**
     * Find existing OAuth2 user or create new one
     */
    private User findOrCreateOAuth2User(String provider, String oauth2Id, String email, String name, String pictureUrl) {
        // First, try to find by OAuth2 provider and ID
        Optional<User> existingUser = userRepository.findByOauth2ProviderAndOauth2Id(provider, oauth2Id);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // If not found, try to find by email
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            // Link existing account with OAuth2
            User user = userByEmail.get();
            user.setOauth2Provider(provider);
            user.setOauth2Id(oauth2Id);
            user.setName(name);
            user.setPictureUrl(pictureUrl);
            return userRepository.save(user);
        }

        // Create new OAuth2 user
        User newUser = new User(email, provider, oauth2Id, name, pictureUrl);
        return userRepository.save(newUser);
    }
} 