package com.semih.common.exception;

public class MissingAuthorizationHeaderException extends RuntimeException{

    public MissingAuthorizationHeaderException(String message) {
        super(message);
    }
}
