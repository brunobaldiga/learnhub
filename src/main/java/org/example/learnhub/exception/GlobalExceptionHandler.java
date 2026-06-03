package org.example.learnhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CourseAccessDenied.class)
    public ResponseEntity<String> handle(CourseAccessDenied ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFound.class)
    public ResponseEntity<String> handle(EntityNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    @ExceptionHandler({
            EmailAlreadyInUse.class,
            MaxSectionsReached.class,
            UserAlreadyEnrolled.class,
            UsernameAlreadyInUse.class
    })
    public ResponseEntity<String> handle(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handle(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to perform this action");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generic(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
    }
}
