package org.example.learnhub.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationApiError(
        int status,
        List<ValidationError> errors,
        LocalDateTime timestamp
) {}
