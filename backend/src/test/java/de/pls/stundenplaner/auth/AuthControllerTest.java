package de.pls.stundenplaner.auth;

import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthController controller;

    private User mockUser;
    private UUID userUUID;

    @BeforeEach
    void setUp() {
        userUUID = UUID.randomUUID();
        mockUser = mock(User.class);
    }

    // -- Logins -- //

    @Test
    void login_validCredentials_returnsOk() {
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(request.getSession(true)).thenReturn(session);
        when(mockUser.getUserUUID()).thenReturn(userUUID);

        ResponseEntity<String> response = controller.login(loginRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(session).setAttribute("AUTHENTICATED", true);
        verify(session).setAttribute("USER_UUID", userUUID);
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws InvalidLoginException {
        LoginRequest loginRequest = new LoginRequest("user", "wrongPassword");
        doThrow(new InvalidLoginException()).when(authService).checkLogin(loginRequest);

        ResponseEntity<String> response = controller.login(loginRequest, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(request, never()).getSession(anyBoolean());
    }

    @Test
    void login_userNotFoundAfterCheck_returnsUnauthorized() {
        LoginRequest loginRequest = new LoginRequest("ghost", "pass");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.login(loginRequest, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(request, never()).getSession(anyBoolean());
    }

    // -- Registers -- //

    @Test
    void register_newUser_returnsOk() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest registerRequest = new RegisterRequest("newUser", "pass");

        ResponseEntity<String> response = controller.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).registerUser(registerRequest);
    }

    @Test
    void register_existingUser_returnsConflict() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest registerRequest = new RegisterRequest("existingUser", "pass");
        doThrow(new UserAlreadyExistsException()).when(authService).registerUser(registerRequest);

        ResponseEntity<String> response = controller.register(registerRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void register_emptyUsername_returnsUnauthorized() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest registerRequest = new RegisterRequest("", "pass");
        doThrow(new EmptyUsernameException()).when(authService).registerUser(registerRequest);

        ResponseEntity<String> response = controller.register(registerRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // -- checks -- //

    @Test
    void checkSession_validAuthenticatedSession_returnsTrue() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("AUTHENTICATED")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.checkSession(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    void checkSession_sessionExistsButNotAuthenticated_returnsFalse() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("AUTHENTICATED")).thenReturn(false);

        ResponseEntity<Boolean> response = controller.checkSession(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }

    @Test
    void checkSession_noSession_returnsFalse() {
        when(request.getSession(false)).thenReturn(null);

        ResponseEntity<Boolean> response = controller.checkSession(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }

    @Test
    void checkSession_sessionAttributeNull_returnsFalse() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("AUTHENTICATED")).thenReturn(null);

        ResponseEntity<Boolean> response = controller.checkSession(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }
}