package com.application.eurder2.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public static ConflictException emailAlreadyInUse() {
        return new ConflictException("This email is already in use.");
    }

    public static ConflictException phoneNumberAlreadyInUse() {
        return new ConflictException("This phone number is already in use.");
    }
}