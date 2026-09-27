package org.example.learnhub.exception;

public class MaxLessonsReachedException extends RuntimeException {
    public MaxLessonsReachedException(String message) {
        super(message);
    }
}
