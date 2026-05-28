package org.example.learnhub.exception;

public class UserAlreadyEnrolled extends RuntimeException {
    public UserAlreadyEnrolled(String message) {
        super(message);
    }
}
