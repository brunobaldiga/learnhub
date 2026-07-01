package org.example.learnhub.exception;

public class CourseReviewNotAllowedException extends RuntimeException {
    public CourseReviewNotAllowedException(String message) {
        super(message);
    }
}
