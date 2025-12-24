package com.group18.greengrocer.service;

/**
 * Exception thrown when a business rule or validation check fails.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
