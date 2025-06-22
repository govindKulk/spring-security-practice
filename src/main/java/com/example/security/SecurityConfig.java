package com.example.security;

import com.example.security.jwt.JwtAuthenticationFilter;
import com.example.security.oauth2.OAuth2SuccessHandler;
import com.example.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

/**
 * Spring Security Configuration Class - Updated for JWT with Refresh Tokens
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
/* EnableMethodSecurity is used to enable method security
 * prePostEnabled indicates that the @PreAuthorize and @PostAuthorize annotations are enabled
 */
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private OAuth2SuccessHandler oauth2SuccessHandler;

    /**
     * SecurityFilterChain Bean - Configured for JWT
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/public/**").permitAll()
                
                // Auth endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                
                // Role-based endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/moderator/**").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN", "MODERATOR")
                
                // Private endpoints - requires any authentication
                .requestMatchers("/private/**").authenticated()
                
                // H2 console - permit for development (disable in production)
                .requestMatchers("/h2-console/**").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // OAuth2 configuration
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2SuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(this.oidcUserService())
                    .userService(this.oauth2UserService())
                )
            )
            
            // Disable form login for JWT
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic Auth for JWT
            .httpBasic(basic -> basic.disable())
            
            // Configure session management for stateless JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Disable CSRF for JWT (development only) - FIXED: Disable completely for JWT
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/auth/**", "/oauth2/**")
            )
            
            // Allow H2 console frames (development only)
            .headers(headers -> headers
                .frameOptions().disable()
            );
        
        return http.build();
    }

    /**
     * OIDC User Service for Google OAuth2
     */
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);

            Map<String, Object> attributes = oidcUser.getAttributes();
            Map<String, Object> claims = oidcUser.getClaims();

            return new DefaultOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub" // Use "sub" as the name attribute
            );
        };
    }

    /**
     * OAuth2 User Service for non-OIDC providers
     */
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return (userRequest) -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);

            Map<String, Object> attributes = oauth2User.getAttributes();

            return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                "sub" // Use "sub" as the name attribute
            );
        };
    }

    /**
     * Password Encoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider Bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}