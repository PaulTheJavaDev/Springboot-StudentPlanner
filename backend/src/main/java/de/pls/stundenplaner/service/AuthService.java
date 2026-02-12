package de.pls.stundenplaner.service;

import de.pls.stundenplaner.dto.request.auth.LoginRequest;
import de.pls.stundenplaner.dto.request.auth.RegisterRequest;
import de.pls.stundenplaner.dto.response.auth.LoginResponse;
import de.pls.stundenplaner.repository.UserRepository;
import de.pls.stundenplaner.model.User;
import de.pls.stundenplaner.util.PasswordHasher;
import de.pls.stundenplaner.util.exceptions.InvalidLoginException;
import de.pls.stundenplaner.util.exceptions.UserAlreadyExistsException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
     * @return A DTO for the Login {@code response} part which returns the SessionID for the Frontend to handle.
     * @throws InvalidLoginException Thrown if the credentials from the request DTO are invalid.
     */
    public LoginResponse checkLogin(
            final @NotNull @NonNull LoginRequest loginRequest
    ) throws InvalidLoginException {

        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(InvalidLoginException::new);

        String hashedInputPassword = PasswordHasher.sha256(loginRequest.password());

        if (!hashedInputPassword.equals(user.getPassword_hash())) {
            throw new InvalidLoginException();
        }

        final UUID sessionID = UUID.randomUUID();
        user.setSessionID(sessionID);
        userRepository.save(user);

        return new LoginResponse(sessionID);
    }

    /**
     * Validates if the User with its given credentials is able to register.
     *
     * @param registerRequest A DTO for the Login {@code request} part which holds the Username and Password.
     * @throws UserAlreadyExistsException Thrown when the username from the credentials already exists.
     */
    public void registerUser(
            final @NotNull @NonNull RegisterRequest registerRequest
    ) throws UserAlreadyExistsException {

        final String username = registerRequest.username();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }

        final String hashedPassword = PasswordHasher.sha256(registerRequest.password());

        final User user = new User(
                username,
                hashedPassword
        );

        userRepository.save(user);
    }
}
