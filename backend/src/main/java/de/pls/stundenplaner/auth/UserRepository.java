package de.pls.stundenplaner.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Grants Database access to the User Column
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    Optional<User> findBySessionID(UUID sessionID);
    Optional<User> findByUserUUID(UUID userUUID);

}