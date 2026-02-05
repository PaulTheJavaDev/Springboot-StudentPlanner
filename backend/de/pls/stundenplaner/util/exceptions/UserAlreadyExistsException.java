package de.pls.stundenplaner.util.exceptions;

public class UserAlreadyExistsException extends Exception {

    /**
     * Creates a {@link UserAlreadyExistsException} and responds with
     * @param username Username of the already existing User
     */
    public UserAlreadyExistsException(String username) {
        super("User '" + username + "' already exists.");
    }

}
