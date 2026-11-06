package com.application.eurder2.exception;

public class ExistenceException extends RuntimeException {

    public ExistenceException(String message) {
        super(message);
    }

    public static ExistenceException notInDatabase(String objectType) {
        return new ExistenceException("This " + objectType + " is not found in the database.");
    }
}
