package com.example.security.repository;

import com.example.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository Interface
 * 
 * This interface extends JpaRepository to provide database operations for the User entity.
 * Spring Data JPA automatically implements this interface and provides common CRUD operations.
 * 
 * Key Features:
 * - Extends JpaRepository<User, Long> for basic CRUD operations
 * - Provides custom query methods using Spring Data JPA naming conventions
 * - Automatically implemented by Spring Data JPA
 * 
 * Available Operations:
 * - save(User user) - Save or update a user
 * - findById(Long id) - Find user by ID
 * - findAll() - Find all users
 * - delete(User user) - Delete a user
 * - findByUsername(String username) - Custom method to find by username
 * 
 * Spring Data JPA automatically creates the implementation based on method names.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     * 
     * Spring Data JPA automatically creates a query like:
     * SELECT * FROM users WHERE username = ?
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * Spring Data JPA automatically creates the implementation
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists with the given email.
     * 
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
} 