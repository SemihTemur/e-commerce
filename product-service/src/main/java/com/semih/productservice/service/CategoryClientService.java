package com.semih.productservice.service;

import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryAndSubCategoryRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import com.semih.productservice.client.CategoryClient;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.entity.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryClientService {

    private final CategoryClient categoryClient;

    private static final Logger log = LoggerFactory.getLogger(CategoryClientService.class);

    public CategoryClientService(CategoryClient categoryClient) {
        this.categoryClient = categoryClient;
    }

    @CircuitBreaker(
            name = "categoryService",
            fallbackMethod = "categoryValidationFallback"
    )
    public void validateCategories(List<CategoryValidationRequest> categoryRequestList) {
        categoryClient.validateCategoryHierarchy(categoryRequestList);
    }

    public void categoryValidationFallback(
            List<CategoryValidationRequest> categoryRequestList,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while validating categories. requests={}",
                categoryRequestList,
                t
        );

        throw new RuntimeException("Category service is temporarily unavailable");
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "validateCategoryExistsFallback")
    public void validateCategoryExists(Long categoryId){
        categoryClient.validateCategoryExistsById(categoryId);
    }

    public void validateCategoryExistsFallback(
            Long categoryId,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while validating category. categoryId={}",
                categoryId,
                t
        );

        throw new RuntimeException("Category service is temporarily unavailable");
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "validateSubCategoryExistsFallback")
    public void validateSubCategoryExists(Long categoryId, Long subCategoryId){
        categoryClient.validateSubCategoryExists(categoryId, subCategoryId);
    }

    public void validateSubCategoryExistsFallback(
            Long categoryId,
            Long subCategoryId,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while validating subcategory. categoryId={}, subCategoryId={}",
                categoryId,
                subCategoryId,
                t
        );

        throw new RuntimeException("Category service is temporarily unavailable");
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "fetchCategoriesWithSubCategoriesFallback")
    public List<ProductCategoryInfoResponse> fetchCategoriesWithSubCategories(
            List<ProductCategoryAndSubCategoryRequest> requests){
        return categoryClient
                .getCategoryWithSubCategoriesForProductList(requests)
                .getBody();
    }

    public List<ProductCategoryInfoResponse> fetchCategoriesWithSubCategoriesFallback(
            List<ProductCategoryAndSubCategoryRequest> requests,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while fetching categories. requestCount={}",
                requests != null ? requests.size() : 0,
                t
        );

        return List.of(); // default response
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "validateCategoryStructureCategoryServiceFallback")
    public void validateCategoryHierarchy(List<CategoryValidationRequest> categoryRequestList) {
        categoryClient.validateCategoryHierarchy(categoryRequestList);
    }

    public void validateCategoryStructureCategoryServiceFallback(
            ProductRequest request,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while validating category structure. request={}",
                request,
                t
        );
        throw new RuntimeException("Category service is temporarily unavailable");
    }


}
