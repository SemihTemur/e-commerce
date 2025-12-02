package com.semih.productservice.exception;

import com.semih.common.dto.response.ApiError;

public class NotFoundException extends RuntimeException{

   private ApiError apiError;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(ApiError apiError) {
        this.apiError = apiError;
    }

    public NotFoundException(String message, ApiError apiError) {
        super(message);
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
