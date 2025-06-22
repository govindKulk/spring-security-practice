package com.example.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Moderator Controller with Role-Based Access Control
 */
@RestController
@RequestMapping("/moderator")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')") // All methods require ADMIN or MODERATOR role
public class ModeratorController {

    /**
     * Get moderator dashboard
     * Access: ADMIN or MODERATOR role
     */
    @GetMapping("/dashboard")
    public ResponseEntity<String> getModeratorDashboard() {
        return ResponseEntity.ok("Moderator Dashboard - Content Management");
    }

    /**
     * Moderate content
     * Access: ADMIN or MODERATOR role
     */
    @PostMapping("/content/{id}/moderate")
    public ResponseEntity<String> moderateContent(@PathVariable Long id, @RequestParam String action) {
        return ResponseEntity.ok("Content " + id + " moderated with action: " + action);
    }

    /**
     * Get pending content
     * Access: ADMIN or MODERATOR role
     */
    @GetMapping("/content/pending")
    public ResponseEntity<String> getPendingContent() {
        return ResponseEntity.ok("Pending content list");
    }

    /**
     * Approve content
     * Access: MODERATOR role only (not ADMIN)
     */
    @PostMapping("/content/{id}/approve")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<String> approveContent(@PathVariable Long id) {
        return ResponseEntity.ok("Content " + id + " approved");
    }
} 