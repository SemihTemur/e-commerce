package com.semih.categoryservice.controller;

import com.semih.categoryservice.dto.request.CategoryRequest;
import com.semih.categoryservice.dto.response.CategoryResponse;
import com.semih.categoryservice.dto.response.CategoryWithSubCategoriesResponse;
import com.semih.categoryservice.service.CategoryService;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryAndSubCategoryRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(CATEGORIES)
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(categoryService.createCategory(request));
    }


    @PostMapping(VALIDATE_CATEGORY_HIERARCHY)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> validateCategoryHierarchy(
            @RequestBody List<CategoryValidationRequest> requests) {

        categoryService.validateCategoryHierarchy(requests);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(EXISTS_WITH_SUBCATEGORIES)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> existsCategoryWithSubCategories(
            @RequestBody CategoryValidationRequest request) {

        categoryService.existsCategoryWithSubCategories(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return ResponseEntity.ok(categoryService.getCategoryList());
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CategoryWithSubCategoriesResponse> getCategoryWithSubCategoriesById(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(categoryService.getCategoryWithSubCategoriesById(categoryId));
    }

    @PostMapping(FOR_PRODUCT)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProductCategoryInfoResponse>> getCategoryWithSubCategoriesForProduct(
            @RequestBody List<ProductCategoryAndSubCategoryRequest> requests) {

        return ResponseEntity.ok(
                categoryService.getCategoryWithSubCategoriesForProductList(requests)
        );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryById(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(categoryService.updateCategoryById(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Boolean> deleteCategoryById(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.deleteCategoryById(categoryId));
    }
}
