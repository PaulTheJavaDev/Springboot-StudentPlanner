package de.pls.stundenplaner.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import de.pls.stundenplaner.auth.AuthService;
import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.dto.response.auth.LoginResponse;
import de.pls.stundenplaner.util.PasswordHasher;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;

@SuppressWarnings("unused")
public class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private AuthService service;

    @BeforeEach
    private void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUserWithValidCredentials() throws EmptyUsernameException, UserAlreadyExistsException {

        User user = new User(
            "Username", 
            "Password"
            );

        RegisterRequest registerRequest = new RegisterRequest(
            user.getUsername(),
            user.getPassword_hash()
        );

        service.registerUser(registerRequest);

    }

    @Test
    void createUserWithAlreadyExistingUsername() {

        try {

            User user = new User(
                "Username", 
                "Password"
            );

            RegisterRequest registerRequest = new RegisterRequest(
                user.getUsername(),
                user.getPassword_hash()
            );

            service.registerUser(registerRequest);

        } catch (UserAlreadyExistsException | EmptyUsernameException e ) {
            assertEquals(UserAlreadyExistsException.class, e.getClass());
        }

    }

    @Test
    void createUserWithExistingUsername() {

        try {

            User user = new User(
                "", 
                "Password"
            );

            RegisterRequest registerRequest = new RegisterRequest(
                user.getUsername(),
                user.getPassword_hash()
            );

            service.registerUser(registerRequest);

        } catch (UserAlreadyExistsException | EmptyUsernameException e ) {
            assertEquals(EmptyUsernameException.class, e.getClass());
        }

    }

    @Test
    void createUserThenLoginWithSuccess() throws EmptyUsernameException, InvalidLoginException, UserAlreadyExistsException {
        String username = "bhjkjjgdse57ijhgz";
        String password = "Password";
        
        // Mock the repository to return empty initially (user doesn't exist yet)
        when(repository.findByUsername(username)).thenReturn(Optional.empty());
        
        // Register
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        service.registerUser(registerRequest);
        
        // Now mock the repository to return the saved user with HASHED password
        User savedUser = new User(username, PasswordHasher.sha256(password));
        when(repository.findByUsername(username)).thenReturn(Optional.of(savedUser));
        
        // Login with plain password (service will hash it)
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse loginResponse = service.checkLogin(loginRequest);
        
        assertNotNull(loginResponse.sessionID());
    }


}
