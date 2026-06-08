package org.example.learnhub.exception;

import org.example.learnhub.exception.dto.ApiError;
import org.example.learnhub.exception.dto.ValidationApiError;
import org.example.learnhub.exception.dto.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            CourseAccessDenied.class,
            AuthorizationDeniedException.class
    })
    public ResponseEntity<ApiError> handle(CourseAccessDenied ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFound.class)
    public ResponseEntity<ApiError> handle(EntityNotFound ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            EmailAlreadyInUse.class,
            MaxSectionsReached.class,
            UserAlreadyEnrolled.class,
            UsernameAlreadyInUse.class
    })
    public ResponseEntity<ApiError> handle(RuntimeException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationApiError> handle(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                )).toList();

        return ResponseEntity.badRequest().body(
                new ValidationApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        errors,
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status).body(
                new ApiError(
                        status.value(),
                        message,
                        LocalDateTime.now()
                )
        );
    }
}
