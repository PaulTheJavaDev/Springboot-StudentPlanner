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
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
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
    void setUp() throws EmptyUsernameException {
        mockUser = new User("testuser", "stored-hash");
    }

    // -- Check Login -- //

    @Test
    void checkLogin_validCredentials_savesNewSessionID() throws InvalidLoginException {
        LoginRequest request = new LoginRequest("testuser", "password123");
        String hashedPassword = "$2a$10$abcdefghijklmnopqrstuv";

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.isLegacySha256Hash(hashedPassword)).thenReturn(false);
            hasher.when(() -> PasswordHasher.matchesPassword("password123", hashedPassword)).thenReturn(true);
            mockUser.setPassword_hash(hashedPassword);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

            authService.checkLogin(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSessionID()).isNotNull();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
        String hashedPassword = "$2a$10$abcdefghijklmnopqrstuv";

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.isLegacySha256Hash(hashedPassword)).thenReturn(false);
            hasher.when(() -> PasswordHasher.matchesPassword("wrongpassword", hashedPassword)).thenReturn(false);
            mockUser.setPassword_hash(hashedPassword);
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
            hasher.when(() -> PasswordHasher.hashPassword("password123")).thenReturn(hashedPassword);
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

            authService.registerUser(request);

            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void registerUser_usernameAlreadyExists_throwsUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existinguser", "password123");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void registerUser_emptyUsername_throwsEmptyUsernameException() {
        String username = "";
        RegisterRequest request = new RegisterRequest(username, "password123");
        String hashedPassword = "hashed_password";

        try (MockedStatic<PasswordHasher> hasher = mockStatic(PasswordHasher.class)) {
            hasher.when(() -> PasswordHasher.hashPassword("password123")).thenReturn(hashedPassword);
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            assertThrows(EmptyUsernameException.class, () -> authService.registerUser(request));

            verify(userRepository, never()).save(any(User.class));
        }
    }
}