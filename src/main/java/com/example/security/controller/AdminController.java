package com.example.security.controller;

import com.example.security.entity.User;
import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller with Role-Based Access Control
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // All methods in this controller require ADMIN role
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * Get admin dashboard
     * Access: ADMIN role only
     */
    @GetMapping("/dashboard")
    public ResponseEntity<String> getAdminDashboard() {
        return ResponseEntity.ok("Admin Dashboard - System Overview");
    }

    /**
     * Get all users
     * Access: ADMIN role only
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     * Access: ADMIN role only
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user role
     * Access: ADMIN role only
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateUserRole(id, role);
        return ResponseEntity.ok("User role updated successfully");
    }

    /**
     * Delete user
     * Access: ADMIN role only
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    /**
     * System statistics
     * Access: ADMIN role only
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getSystemStats() {
        return ResponseEntity.ok("System Statistics: Users: 100, Active Sessions: 50");
    }
} 