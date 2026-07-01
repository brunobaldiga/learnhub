package org.example.learnhub.exception;

public class DuplicatePurchaseException extends RuntimeException {
    public DuplicatePurchaseException(String message) {
        super(message);
    }
}
