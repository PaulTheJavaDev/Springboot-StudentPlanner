package de.pls.stundenplaner.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    void hashPassword_matchesPassword_returnsTrue() {
        String password = "password";
        String hash = PasswordHasher.hashPassword(password);
        assertTrue(PasswordHasher.matchesPassword(password, hash));
    }

}
