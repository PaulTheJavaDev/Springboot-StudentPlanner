package de.pls.stundenplaner.util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    private final int FORBIDDEN_STATUS_CODE = HttpStatus.FORBIDDEN.value();
    private final int BAD_REQUEST_STATUS_CODE =  HttpStatus.BAD_REQUEST.value();

    @ExceptionHandler(EmptyUsernameException.class)
    public ResponseEntity<Void> handleAlreadyExistingUsername() {
        return ResponseEntity.status(BAD_REQUEST_STATUS_CODE).build();
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<Void> handleInvalidLogin() {
        return ResponseEntity.status(BAD_REQUEST_STATUS_CODE).build();
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<Void> handleInvalidSession() {
        return ResponseEntity.status(FORBIDDEN_STATUS_CODE).build();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Void> handleUnauthorizedAccess() {
        return ResponseEntity.status(FORBIDDEN_STATUS_CODE).build();
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Void> handleUserAlreadyExists() {
        return ResponseEntity.status(BAD_REQUEST_STATUS_CODE).build();
    }

}