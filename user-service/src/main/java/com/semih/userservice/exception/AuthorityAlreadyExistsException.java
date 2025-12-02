package com.semih.userservice.exception;

public class AuthorityAlreadyExistsException extends RuntimeException {

    public AuthorityAlreadyExistsException(String message) {
        super(message);
    }
}
