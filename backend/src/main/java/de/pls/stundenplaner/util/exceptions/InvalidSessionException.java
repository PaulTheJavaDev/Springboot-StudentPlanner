package de.pls.stundenplaner.util.exceptions;

/**
 * Thrown when a user request contains no Session-ID or an invalid Session-ID.
 * <p>
 * Most Use-cases for this Exception to be thrown are in Classes which interact with Http-related Objects and some logic fails.
 * </p>
 *
 * Common reasons:
 * <ul>
 *     <li>Missing Session ID</li>
 *     <li>Invalid Session ID</li>
 * </ul>
 *
 * Most Use-cases for this Exception to be thrown are in Classes which interact with Http-related Objects.
 *
 */
public class InvalidSessionException extends Exception {

    /**
     * Creates an {@link InvalidSessionException} with a default error message.
     */
    public InvalidSessionException() {
        super("Invalid SessionID.");
    }

    public InvalidSessionException(String message) {
        super(message);
    }

}
