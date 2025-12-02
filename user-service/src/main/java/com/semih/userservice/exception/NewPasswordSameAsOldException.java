package com.semih.userservice.exception;

public class NewPasswordSameAsOldException extends RuntimeException {

    public NewPasswordSameAsOldException(String message) {
        super(message);
    }
}
