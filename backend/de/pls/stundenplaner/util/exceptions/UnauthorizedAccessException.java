package de.pls.stundenplaner.util.exceptions;

/**
 * Thrown when a user request is not authorized to do a certain action.
 * <p>
 * This exception is only used when accessing endpoints
 * that require valid authenticated access.
 * </p>
 *
 * Common reasons:
 * <ul>
 *     <li>Invalid or wrong UserUUID</li>
 *     <li>Invalid Session ID</li>
 * </ul>
 *
 */
public class UnauthorizedAccessException extends Exception {

    /**
     * Creates an {@link UnauthorizedAccessException} with a default error message.
     */
    public UnauthorizedAccessException() {
        super("Not authorized to update this assignment");
    }

    /**
     * Creates an {@link UnauthorizedAccessException} with a custom error message.
     *
     * @param message detailed error description
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

}

