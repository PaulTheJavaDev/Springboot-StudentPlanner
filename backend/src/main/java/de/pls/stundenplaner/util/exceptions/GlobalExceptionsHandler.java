package de.pls.stundenplaner.util.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<Void> handleInvalidSession() {
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Void> handleUnauthorized() {
        return ResponseEntity.status(403).build();
    }
}