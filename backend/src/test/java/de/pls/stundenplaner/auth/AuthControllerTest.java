package de.pls.stundenplaner.auth;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.dto.response.auth.LoginResponse;
import de.pls.stundenplaner.dto.response.auth.RegisterResponse;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private FakeAuthService authService;
    private AuthController controller;

    private User mockUser;
    private UUID userUUID;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() throws EmptyUsernameException {
        authService = new FakeAuthService();
        controller = new AuthController(authService, userRepository);
        userUUID = UUID.randomUUID();
        mockUser = new User("auth-user", "hash");
        mockUser.setUserUUID(userUUID);
    }

    // -- Logins -- //

    @Test
    void login_validCredentials_returnsOk() {
        LoginRequest loginRequest = new LoginRequest("user", "pass");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(request.getSession(true)).thenReturn(session);

        ResponseEntity<LoginResponse> response = controller.login(loginRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(session).setAttribute("AUTHENTICATED", true);
        verify(session).setAttribute("USER_UUID", userUUID);
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws InvalidLoginException, NoSuchAlgorithmException {
        LoginRequest loginRequest = new LoginRequest("user", "wrongPassword");
        authService.loginException = new InvalidLoginException();

        ResponseEntity<LoginResponse> response = controller.login(loginRequest, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(request, never()).getSession(anyBoolean());
    }

    @Test
    void login_userNotFoundAfterCheck_returnsUnauthorized() {
        LoginRequest loginRequest = new LoginRequest("ghost", "pass");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseEntity<LoginResponse> response = controller.login(loginRequest, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(request, never()).getSession(anyBoolean());
    }

    // -- Registers -- //

    @Test
    void register_newUser_returnsOk() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest registerRequest = new RegisterRequest("newUser", "pass");
        authService.registerResponse = new RegisterResponse(UUID.randomUUID());

        ResponseEntity<RegisterResponse> response = controller.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void register_existingUser_returnsConflict() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest registerRequest = new RegisterRequest("existingUser", "pass");
        authService.registerException = new UserAlreadyExistsException();

        ResponseEntity<RegisterResponse> response = controller.register(registerRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void register_emptyUsername_returnsBadRequest() throws UserAlreadyExistsException, EmptyUsernameException {
        RegisterRequest registerRequest = new RegisterRequest("", "pass");
        authService.registerException = new EmptyUsernameException();

        ResponseEntity<RegisterResponse> response = controller.register(registerRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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

    private static final class FakeAuthService extends AuthService {
        private LoginResponse loginResponse;
        private Exception loginException;
        private RegisterResponse registerResponse;
        private Exception registerException;

        private FakeAuthService() {
            super(null);
        }

        @Override
        public LoginResponse checkLogin(LoginRequest loginRequest) throws InvalidLoginException, NoSuchAlgorithmException {
            if (loginException != null) {
                if (loginException instanceof InvalidLoginException invalidLoginException) {
                    throw invalidLoginException;
                }
                if (loginException instanceof NoSuchAlgorithmException noSuchAlgorithmException) {
                    throw noSuchAlgorithmException;
                }
            }
            return loginResponse != null ? loginResponse : new LoginResponse(UUID.randomUUID());
        }

        @Override
        public RegisterResponse registerUser(RegisterRequest registerRequest) throws UserAlreadyExistsException, EmptyUsernameException {
            if (registerException != null) {
                if (registerException instanceof UserAlreadyExistsException userAlreadyExistsException) {
                    throw userAlreadyExistsException;
                }
                if (registerException instanceof EmptyUsernameException emptyUsernameException) {
                    throw emptyUsernameException;
                }
            }
            return registerResponse != null ? registerResponse : new RegisterResponse(UUID.randomUUID());
        }
    }
}