package org.example.learnhub.exception.dto;

public record ValidationError(
        String field,
        String message
) {
}
