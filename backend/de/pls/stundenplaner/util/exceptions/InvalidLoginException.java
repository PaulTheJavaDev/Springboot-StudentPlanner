package de.pls.stundenplaner.util.exceptions;

/**
 * Thrown when a login request contains an invalid username or password.
 * <p>
 * This exception is only used when accessing endpoints
 * that require a valid authenticated session.
 * </p>
 *
 * Common reasons:
 * <ul>
 *     <li>Invalid or non-existent Username</li>
 *     <li>Invalid Password</li>
 * </ul>
 *
 */
public class InvalidLoginException extends Exception {

    /**
     * Creates an {@link InvalidLoginException} with a default error message.
     */
    public InvalidLoginException() {
        super("Invalid username or password.");
    }

    /**
     * Creates an {@link InvalidLoginException} with a custom error message.
     *
     * @param message detailed error description
     */
    public InvalidLoginException(String message) {
        super(message);
    }

}
