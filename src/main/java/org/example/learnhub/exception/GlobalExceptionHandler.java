package org.example.learnhub.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.learnhub.exception.dto.ApiError;
import org.example.learnhub.exception.dto.ValidationApiError;
import org.example.learnhub.exception.dto.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({
            CourseAccessDeniedException.class,
            ReviewOwnershipException.class,
            AuthorizationDeniedException.class,
            CourseNotCompletedException.class
    })
    public ResponseEntity<ApiError> handleForbidden(Exception ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFound.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFound ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
    }

    @ExceptionHandler({
            EmailAlreadyInUse.class,
            MaxSectionsReached.class,
            UserAlreadyEnrolled.class,
            UsernameAlreadyInUse.class,
            DuplicatePurchaseException.class,
            DuplicateReviewException.class,
            DuplicateCertificateException.class,
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            SelfReviewNotAllowedException.class,
            CourseReviewNotAllowedException.class,
            InvalidLessonProgressException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.badRequest().body(
                new ValidationApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        errors,
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected exception", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error occurred"
        );
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                new ApiError(
                        status.value(),
                        message,
                        LocalDateTime.now()
                )
        );
    }
}