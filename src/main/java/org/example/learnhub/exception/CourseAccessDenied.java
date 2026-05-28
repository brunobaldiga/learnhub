package org.example.learnhub.exception;

public class CourseAccessDenied extends RuntimeException {
    public CourseAccessDenied(String message) {
        super(message);
    }
}
