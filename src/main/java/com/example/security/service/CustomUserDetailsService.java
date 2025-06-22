package com.example.security.service;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom User Details Service
 * 
 * This service implements Spring Security's UserDetailsService interface to load users
 * from the database for authentication. It bridges our database User entity with
 * Spring Security's authentication system.
 * 
 * Key Features:
 * - Implements UserDetailsService interface
 * - Loads users from database using UserRepository
 * - Handles UsernameNotFoundException when user not found
 * - Uses @Transactional for database operations
 * 
 * Spring Security Integration:
 * - Spring Security calls loadUserByUsername() during authentication
 * - Returns UserDetails object (our User entity implements this)
 * - Throws UsernameNotFoundException if user doesn't exist
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

   
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by their username.
     * 
     * This method is called by Spring Security during the authentication process.
     * It searches the database for a user with the given username and returns
     * a UserDetails object that Spring Security can use for authentication.
     * 
     * @param username the username to search for
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in database by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Log successful user loading (for debugging)
        System.out.println("Loaded user: " + user.getUsername() + " with roles: " + user.getRoles());

        // Return the user (our User entity implements UserDetails)
        return user;
    }

    /**
     * Create a new user in the database.
     * 
     * This is a utility method for creating users programmatically.
     * 
     * @param user the user to create
     * @return the saved user with generated ID
     */
    public User createUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Check if a user exists with the given username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }


    
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return user;
    }
} 