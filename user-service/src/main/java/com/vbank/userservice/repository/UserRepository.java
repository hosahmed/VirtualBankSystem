package com.vbank.userservice.repository;

import com.vbank.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring generates the SQL from the method signature,
 * no @Query annotation needed for these simple lookups.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
