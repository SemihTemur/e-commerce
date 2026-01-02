package com.semih.basketservice.exception;

public class BasketNotFoundException extends RuntimeException{

    public BasketNotFoundException(String message) {
        super(message);
    }
}
