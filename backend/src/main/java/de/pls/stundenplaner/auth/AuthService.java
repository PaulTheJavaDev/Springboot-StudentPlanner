package de.pls.stundenplaner.auth;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.util.PasswordHasher;
import de.pls.stundenplaner.util.exceptions.EmptyUsernameException;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;
import lombok.NonNull;

/**
 * Handles the Authentication process.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates if the User with its given credentials is able to log in.
     *
     * @param loginRequest A DTO for the Login {@code request} part which holds the Username and Password.
     * @throws InvalidLoginException Thrown if the credentials from the request DTO are invalid.
     */
    public void checkLogin(
            final @NonNull LoginRequest loginRequest
    ) throws InvalidLoginException {

        final User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(InvalidLoginException::new);

        final String hashedInputPassword = PasswordHasher.sha256(loginRequest.password());

        if (!hashedInputPassword.equals(user.getPassword_hash())) {
            throw new InvalidLoginException();
        }

        final UUID sessionID = UUID.randomUUID();
        user.setSessionID(sessionID);
        userRepository.save(user);

    }

    /**
     * Validates if the User with its given credentials is able to register.
     *
     * @param registerRequest A DTO for the Login {@code request} part which holds the Username and Password.
     * @throws UserAlreadyExistsException Thrown when the username from the credentials already exists.
     */
    public void registerUser(
            final @NotNull @NonNull RegisterRequest registerRequest
    ) throws UserAlreadyExistsException, EmptyUsernameException {

        final String username = registerRequest.username();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }

        final String hashedPassword = PasswordHasher.sha256(registerRequest.password());

        if (username.isEmpty()) {
            throw new EmptyUsernameException();
        }

        User user = new User(
                username,
                hashedPassword
        );

        userRepository.save(user);
    }
}
