package com.example.security.service;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * User Service with Role-Based Operations
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    /**
     * Update user role (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserRole(Long userId, String role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.getRoles().clear();
            user.addRole(role);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    /**
     * Delete user (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    /**
     * Get user count (ADMIN only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    public long getUserCount() {
        return userRepository.count();
    }
} 