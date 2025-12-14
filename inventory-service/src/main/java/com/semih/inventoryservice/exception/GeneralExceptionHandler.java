package com.semih.inventoryservice.exception;

import com.semih.common.dto.response.ApiError;
import com.semih.common.exception.StockNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler({StockNotFoundException.class})
    public ResponseEntity<ApiError> handleStockException(StockNotFoundException ex) {
        ApiError apiError = new ApiError(404, ex.getMessage(), OffsetDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
}
