package com.semih.productservice.client;

import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.semih.productservice.config.RestApis.*;

@FeignClient(name="category",url = "http://localhost:8085/dev/v1/category")
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
}
