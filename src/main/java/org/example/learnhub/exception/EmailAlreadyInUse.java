package org.example.learnhub.exception;

public class EmailAlreadyInUse extends RuntimeException {
    public EmailAlreadyInUse(String message) {
        super(message);
    }
}
