package de.pls.stundenplaner.util;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpSessionUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @Test
    void getValidSession_returnsSession_whenAuthenticated() throws InvalidSessionException {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("AUTHENTICATED")).thenReturn(Boolean.TRUE);

        final HttpSession result = HttpSessionUtils.getValidSession(request);

        assertEquals(session, result);
    }

    @Test
    void getUserFromSession_returnsUser_whenSessionValid() throws InvalidSessionException {

        final UUID uuid = UUID.randomUUID();

        when(session.getAttribute("USER_UUID")).thenReturn(uuid);
        when(userRepository.findByUserUUID(uuid)).thenReturn(Optional.of(user));

        final User result = HttpSessionUtils.getUserFromSession(userRepository, session);

        assertEquals(user, result);
    }

}