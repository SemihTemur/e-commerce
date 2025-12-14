package com.semih.basketservice.exception;

public class BasketItemNotFoundException extends RuntimeException{

    public BasketItemNotFoundException(String message) {
        super(message);
    }
}
