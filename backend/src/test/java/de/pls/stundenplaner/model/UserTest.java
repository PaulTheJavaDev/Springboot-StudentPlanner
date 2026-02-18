package de.pls.stundenplaner.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;

public class UserTest {

    @Test
    void testUserCreationWithValidCredentials() throws EmptyUsernameException {

        User user = new User(
                "testuser",
                "hashedpassword123"
        );

        assertEquals(user.getUsername(), "testuser");
        assertEquals(user.getPassword_hash(), "hashedpassword123");
        assertNotNull(user.getUserUUID());

    }

    @Test
    void testUserCreationWithEmptyUsername() {

        try {
            new User(
                    "",
                    "hashedpassword123"
            );
        } catch (Exception e) {
            assertEquals(EmptyUsernameException.class, e.getClass());
        }

    }

    @Test
    void testUserCreationWithNullUsername() {

        try {
            new User(
                    null,
                    "hashedpassword123"
            );
        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

    }

}