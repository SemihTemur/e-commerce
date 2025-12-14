package com.semih.categoryservice.exception;

import com.semih.common.dto.response.ApiError;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String msg = "Bu işlem gerçekleştirilemedi.";
        String text = ex.getMostSpecificCause().getMessage().toLowerCase();

        if (text.contains("category_id")) {
            msg = "Bu kategori zaten ürüne eklenmiş.";
        } else if (text.contains("sub_category_id")) {
            msg = "Bu alt kategori zaten ürüne eklenmiş.";
        } else if (text.contains("uk_sub_category_name_category")) {
            msg = "Bu alt kategori adı zaten mevcut. Lütfen farklı bir ad girin.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(400, msg, OffsetDateTime.now(), null));
    }


    @ExceptionHandler({CategoryNotFoundException.class})
    public ResponseEntity<ApiError> handleCategoryException(CategoryNotFoundException ex) {
        ApiError apiError = new ApiError(404, ex.getMessage(), OffsetDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler({SubCategoryNotFoundException.class})
    public ResponseEntity<ApiError> handleSubCategoryException(SubCategoryNotFoundException ex) {
        ApiError apiError = new ApiError(404, ex.getMessage(), OffsetDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
}
