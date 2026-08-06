package org.example.learnhub.exception;

public class CourseNotCompletedException extends RuntimeException {
    public CourseNotCompletedException(String message) {
        super(message);
    }
}
