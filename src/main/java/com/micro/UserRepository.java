package com.micro;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find a user by their email
    User findByEmail(String email);

    // Check if an email already exists in the database
    boolean existsByEmail(String email);

    // Optional: You may want to include other methods like checking for users by role
    // For example, find users by role (admin, user, etc.)
    List<User> findByRole(String role);
}
