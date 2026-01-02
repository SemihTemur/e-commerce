package com.semih.productservice.client;


import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryAndSubCategoryRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import com.semih.productservice.config.FeignTracingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.common.config.RestApis.*;


@FeignClient(
        name = "CATEGORY-SERVICE",
        path = CATEGORIES,
        configuration = FeignTracingConfig.class
)
public interface CategoryClient {

    @PostMapping(VALIDATE_CATEGORY_HIERARCHY)
    ResponseEntity<Void> validateCategoryHierarchy(@RequestBody List<CategoryValidationRequest> requests);

    @PostMapping(EXISTS_WITH_SUBCATEGORIES)
    ResponseEntity<Void> existsCategoryWithSubCategories(@RequestBody CategoryValidationRequest request);

    @GetMapping(VALIDATE_CATEGORY_EXISTS_BY_ID)
    ResponseEntity<Void> validateCategoryExistsById(@PathVariable Long categoryId);

    @GetMapping(FOR_PRODUCT)
    ResponseEntity<List<ProductCategoryInfoResponse>> getCategoryWithSubCategoriesForProductList(
            @RequestBody List<ProductCategoryAndSubCategoryRequest> requests
    );

    @GetMapping(SUBCATEGORIES + VALIDATE_CATEGORY_EXISTS_BY_ID)
    ResponseEntity<Void> validateSubCategoryExists(@PathVariable Long categoryId, @PathVariable Long subCategoryId);
}
