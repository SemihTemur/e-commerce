package com.semih.productservice.exception;

import com.semih.common.dto.response.ApiError;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.StockNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest webRequest) {

        String message = "Bu işlem gerçekleştirilemedi.";

        Throwable root = ex.getMostSpecificCause();
        String errorText = root != null ? root.getMessage().toLowerCase() : "";

        if (errorText.contains("product_category_mapping_product_id_category_id_sub_categor_key")) {
            var request = (ServletWebRequest) webRequest;
            String categoryId = request.getRequest().getParameter("categoryId");
            String subCategoryId = request.getRequest().getParameter("subCategoryId");

            if (categoryId != null && subCategoryId != null) {
                message = "Bu kategori ve alt kategori zaten ürüne eklenmiş.";
            } else if (categoryId != null) {
                message = "Bu kategori zaten ürüne eklenmiş.";
            } else if (subCategoryId != null) {
                message = "Bu alt kategori zaten ürüne eklenmiş.";
            } else {
                message = "Bu kategori bilgisi zaten mevcut.";
            }
        }

        ApiError apiError = new ApiError(
                400,
                message,
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getApiError());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            if (message != null && !message.isBlank()) {
                errors.merge(error.getField(), message, (existing, newMsg) -> {
                    return existing + ", " + newMsg;
                });
            }

        });
        ApiError apiError = new ApiError(400, "Validation error", LocalDateTime.now(), errors);
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductException(ProductNotFoundException ex){
        ApiError apiError = new ApiError(404,ex.getMessage(), LocalDateTime.now(),null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCategoryAlreadyException(CategoryAlreadyExistsException ex){
        ApiError apiError = new ApiError(
                409,
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(SubCategoryAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCategoryAlreadyException(SubCategoryAlreadyExistsException ex){
        ApiError apiError = new ApiError(
                409,
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler({StockNotFoundException.class})
    public ResponseEntity<ApiError> handleStockException(StockNotFoundException ex) {
        ApiError apiError = new ApiError(404, ex.getMessage(), LocalDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler({CategoryNotFoundException.class})
    public ResponseEntity<ApiError> handleCategoryException(CategoryNotFoundException ex) {
        ApiError apiError = new ApiError(404, ex.getMessage(), LocalDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler({SubCategoryNotFoundException.class})
    public ResponseEntity<ApiError> handleSubCategoryException(SubCategoryNotFoundException ex) {
        ApiError apiError = new ApiError(404, ex.getMessage(), LocalDateTime.now(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

}
