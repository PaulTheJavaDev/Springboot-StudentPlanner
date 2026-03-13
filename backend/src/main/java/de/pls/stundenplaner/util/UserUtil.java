package de.pls.stundenplaner.util;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import de.pls.stundenplaner.auth.User;
import de.pls.stundenplaner.auth.UserRepository;
import de.pls.stundenplaner.util.exceptions.InvalidSessionException;
import lombok.NonNull;

/**
 * A class for User-Database related Helper methods.
 */
@Component
public class UserUtil {

    private static UserRepository userRepository;

    public UserUtil(UserRepository userRepository) {
        UserUtil.userRepository = userRepository;
    }

    /**
     * Helper method that acts as a shorter way
     * to access the User found by a SessionID in the Database.
     *
     * @param sessionID A valid SessionID
     * @return A {@link User} Object or {@link InvalidSessionException} when User not found
     * @throws InvalidSessionException Thrown if the SessionID is invalid
     */
    public static User checkUserExistenceBySessionID(
            final @NotNull @NonNull UUID sessionID
    ) throws InvalidSessionException {

        return userRepository.findBySessionID(sessionID)
                .orElseThrow(InvalidSessionException::new);

    }

}
