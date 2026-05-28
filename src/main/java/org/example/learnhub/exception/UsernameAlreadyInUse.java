package org.example.learnhub.exception;

public class UsernameAlreadyInUse extends RuntimeException {
    public UsernameAlreadyInUse(String message) {
        super(message);
    }
}
