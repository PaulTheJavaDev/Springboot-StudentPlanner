package de.pls.stundenplaner.util;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HttpStuff {

    public static HttpSession getValidSession(HttpServletRequest request) throws InvalidSessionException {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("AUTHENTICATED"))) {
            throw new InvalidSessionException();
        }
        return session;
    }

    public static User getUserFromSession(
            final @NotNull UserRepository userRepository,
            final @NotNull HttpSession session
    ) throws InvalidSessionException {

        Object rawUUID = session.getAttribute("USER_UUID");
        if (!(rawUUID instanceof UUID userUUID)) {
            throw new InvalidSessionException("SessionID is not type of: UUID.");
        }

        return userRepository.findByUserUUID(userUUID)
                .orElseThrow(InvalidSessionException::new);

    }

}
