package de.pls.stundenplaner.auth;

import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.util.PasswordHasher;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
    }

    // -- Check Login -- //

    @Test
    void checkLogin_validCredentials_savesNewSessionID() throws InvalidLoginException {
        LoginRequest request = new LoginRequest("testuser", "password123");
        String hashedPassword = "hashed_password";

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.sha256("password123")).thenReturn(hashedPassword);
            when(mockUser.getPassword_hash()).thenReturn(hashedPassword);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

            authService.checkLogin(request);

            verify(mockUser).setSessionID(any());
            verify(userRepository).save(mockUser);
        }
    }

    @Test
    void checkLogin_userNotFound_throwsInvalidLogin() {
        LoginRequest request = new LoginRequest("unknown", "password123");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.checkLogin(request))
                .isInstanceOf(InvalidLoginException.class);

    }

    @Test
    void checkLogin_wrongPassword_throwsInvalidLogin() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.sha256("wrongpassword")).thenReturn("wrong_hash");
            when(mockUser.getPassword_hash()).thenReturn("correct_hash");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

            assertThatThrownBy(() -> authService.checkLogin(request))
                    .isInstanceOf(InvalidLoginException.class);
        }
    }

    // -- Register User -- //

    @Test
    void registerUser_validRequest_savesUser() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest request = new RegisterRequest("newuser", "password123");
        String hashedPassword = "hashed_password";

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.sha256("password123")).thenReturn(hashedPassword);
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

            authService.registerUser(request);

            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void registerUser_usernameAlreadyExists_throwsUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existinguser", "password123");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void registerUser_emptyUsername_throwsEmptyUsernameException() {
        String username = "";
        RegisterRequest request = new RegisterRequest(username, "password123");
        String hashedPassword = "hashed_password";

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.sha256("password123")).thenReturn(hashedPassword);
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            assertThrows(EmptyUsernameException.class, () -> authService.registerUser(request));

            verify(userRepository, never()).save(any(User.class));
        }
    }
}