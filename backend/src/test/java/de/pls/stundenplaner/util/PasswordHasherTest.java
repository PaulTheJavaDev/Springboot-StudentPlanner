package de.pls.stundenplaner.util;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    void test() throws NoSuchAlgorithmException {

        String password = "password";
        String hashedPassword = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";

        String result = PasswordHasher.sha256(password);

        assertEquals(hashedPassword, result);

    }

}
