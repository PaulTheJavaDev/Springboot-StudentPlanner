package de.pls.stundenplaner.util.exceptions;

public class EmptyUsernameException extends Exception {

    public EmptyUsernameException() {
        super("Username cannot be empty");
    }
    
}
