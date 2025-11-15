package com.semih.productservice.exception;

public class SubCategoryAlreadyExistsException extends RuntimeException{

    public SubCategoryAlreadyExistsException(String message) {
        super(message);
    }
}
