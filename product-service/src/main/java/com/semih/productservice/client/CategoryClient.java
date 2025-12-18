package com.semih.productservice.client;

import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import com.semih.productservice.config.FeignTracingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.productservice.config.RestApis.*;

@FeignClient(name="CATEGORY-SERVICE",
        path = CATEGORY,
        configuration = FeignTracingConfig.class)
public interface CategoryClient {

    @PostMapping(VALIDATE_CATEGORY_HIERARCHY)
    ResponseEntity<Void> validateCategoryHierarchy(@RequestBody List<CategoryValidationRequest> categoryValidationRequestList);

    @PostMapping(EXISTS_CATEGORY_WITH_SUBCATEGORIES)
    ResponseEntity<Void> existsCategoryWithSubCategories(@RequestBody CategoryValidationRequest categoryValidationRequestList);

    @GetMapping(VALIDATE_CATEGORY_EXISTS_BY_ID)
    ResponseEntity<Void> validateCategoryExistsById(@PathVariable Long categoryId);

    @GetMapping(GET_CATEGORY_WITH_SUBCATEGORIES_FOR_PRODUCT)
    ResponseEntity<List<ProductCategoryInfoResponse>> getCategoryWithSubCategoriesForProductList
            (@RequestBody List<ProductCategoryInfoRequest> productCategoryInfoRequestList);

    @GetMapping(SUB_CATEGORY+VALIDATE_SUB_CATEGORY_EXISTS_BY_ID)
    ResponseEntity<Void> validateSubCategoryExists(
            @PathVariable Long categoryId, @PathVariable Long subCategoryId);
}
