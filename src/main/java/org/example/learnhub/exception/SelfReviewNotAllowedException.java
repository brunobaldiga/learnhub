package org.example.learnhub.exception;

public class SelfReviewNotAllowedException extends RuntimeException {
    public SelfReviewNotAllowedException(String message) {
        super(message);
    }
}
