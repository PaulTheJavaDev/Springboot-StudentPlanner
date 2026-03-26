package de.pls.stundenplaner.util;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HttpSessionUtils {

    public static HttpSession getValidSession(
            final @NonNull HttpServletRequest request
    ) throws InvalidSessionException {

        final HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            throw new InvalidSessionException();
        }

        return session;
    }

    private static boolean isAuthenticated(final @NonNull HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("AUTHENTICATED"));
    }

    public static User getUserFromSession(
            final @NonNull UserRepository userRepository,
            final @NonNull HttpSession session
    ) throws InvalidSessionException {

        final Object rawUUID = session.getAttribute("USER_UUID");
        if (!(rawUUID instanceof UUID userUUID)) {
            throw new IllegalArgumentException("SessionID is not type of: UUID.");
        }

        return userRepository.findByUserUUID(userUUID)
                .orElseThrow(InvalidSessionException::new);

    }

}
