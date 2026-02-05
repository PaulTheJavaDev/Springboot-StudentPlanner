package de.pls.stundenplaner.util.exceptions;

/**
 * Thrown when a user request contains no session ID or an invalid session ID.
 * <p>
 * This exception is only used when accessing endpoints
 * that require a valid authenticated session.
 * </p>
 *
 * Common reasons:
 * <ul>
 *     <li>Missing Session ID</li>
 *     <li>Invalid Session ID</li>
 * </ul>
 *
 */
public class InvalidSessionException extends Exception {

    /**
     * Creates an {@link InvalidSessionException} with a default error message.
     */
    public InvalidSessionException() {
        super("Invalid SessionID. Please log in to obtain a valid SessionID.");
    }

    /**
     * Creates an {@link InvalidSessionException} with a custom error message.
     *
     * @param message detailed error description
     */
    public InvalidSessionException(String message) {
        super(message);
    }

}
