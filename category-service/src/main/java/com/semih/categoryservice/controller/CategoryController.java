package com.semih.categoryservice.controller;

import com.semih.categoryservice.dto.request.CategoryRequest;
import com.semih.categoryservice.dto.response.CategoryResponse;
import com.semih.categoryservice.dto.response.CategoryWithSubCategoriesResponse;
import com.semih.categoryservice.service.CategoryService;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.categoryservice.config.RestApis.*;

@RestController
@RequestMapping(CATEGORY)
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping(CREATE_CATEGORY)
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest){
        CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);
        return ResponseEntity.ok(categoryResponse);
    }

    @PostMapping(VALIDATE_CATEGORY_HIERARCHY)
    public ResponseEntity<Void> validateCategoryHierarchy(
            @RequestBody List<CategoryValidationRequest> categoryValidationRequestList){
        categoryService.validateCategoryHierarchy(categoryValidationRequestList);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(EXISTS_CATEGORY_WITH_SUBCATEGORIES)
    public ResponseEntity<Void> existsCategoryWithSubCategories(
            @RequestBody CategoryValidationRequest categoryValidationRequest){
        categoryService.existsCategoryWithSubCategories(categoryValidationRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(GET_CATEGORY_LIST)
    public ResponseEntity<List<CategoryResponse>> getCategoryList(){
        List<CategoryResponse> categoryResponseList = categoryService.getCategoryList();
        return ResponseEntity.ok(categoryResponseList);
    }

    @GetMapping(VALIDATE_CATEGORY_EXISTS_BY_ID)
    public ResponseEntity<Void> validateCategoryExistsById(@PathVariable Long categoryId){
        categoryService.validateCategoryExistsById(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(GET_CATEGORY_WITH_SUBCATEGORIES_BY_ID)
    public ResponseEntity<CategoryWithSubCategoriesResponse> getCategoryWithSubCategoriesById(
            @PathVariable Long categoryId){
        CategoryWithSubCategoriesResponse categoryWithSubCategoriesById = categoryService.getCategoryWithSubCategoriesById(categoryId);
        return ResponseEntity.ok(categoryWithSubCategoriesById);
    }

    @PostMapping(GET_CATEGORY_WITH_SUBCATEGORIES_FOR_PRODUCT)
    public ResponseEntity<List<ProductCategoryInfoResponse>> getCategoryWithSubCategoriesForProductList(
            @RequestBody List<ProductCategoryInfoRequest> productCategoryInfoRequests){
        List<ProductCategoryInfoResponse> productCategoryInfoResponses = categoryService
                .getCategoryWithSubCategoriesForProductList(productCategoryInfoRequests);
        return ResponseEntity.ok(productCategoryInfoResponses);
    }

    @GetMapping(GET_ALL_CATEGORY_WITH_SUBCATEGORIES)
    public ResponseEntity<List<CategoryWithSubCategoriesResponse>> getAllCategoryWithSubCategories(){
        List<CategoryWithSubCategoriesResponse> categoryWithSubCategoriesResponseList = categoryService.getAllCategoryWithSubCategories();
        return ResponseEntity.ok(categoryWithSubCategoriesResponseList);
    }

    @PutMapping(UPDATE_CATEGORY)
    public ResponseEntity<CategoryResponse> updateCategoryById(
            @PathVariable Long categoryId, @Valid @RequestBody CategoryRequest categoryRequest){
        CategoryResponse categoryResponse = categoryService.updateCategoryById(categoryId,categoryRequest);
        return ResponseEntity.ok(categoryResponse);
    }

    @DeleteMapping(DELETE_CATEGORY)
    public ResponseEntity<Boolean> deleteCategoryById(@PathVariable Long categoryId){
        return ResponseEntity.ok(categoryService.deleteCategoryById(categoryId));
    }

}
