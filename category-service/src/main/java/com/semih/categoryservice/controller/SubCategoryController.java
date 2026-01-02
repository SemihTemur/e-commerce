package com.semih.categoryservice.controller;

import com.semih.categoryservice.dto.request.SubCategoryCreateRequest;
import com.semih.categoryservice.dto.request.SubCategoryUpdateRequest;
import com.semih.categoryservice.dto.response.SubCategoryResponse;
import com.semih.categoryservice.service.SubCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(SUBCATEGORIES)
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    public SubCategoryController(SubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }

    @PostMapping
    public ResponseEntity<SubCategoryResponse> createSubCategory(
            @Valid @RequestBody SubCategoryCreateRequest request) {

        return ResponseEntity.ok(subCategoryService.createSubCategory(request));
    }

    @GetMapping
    public ResponseEntity<List<SubCategoryResponse>> getSubCategoryList() {
        return ResponseEntity.ok(subCategoryService.getSubCategoryList());
    }

    @GetMapping("/{subCategoryId}")
    public ResponseEntity<SubCategoryResponse> getSubCategoryById(
            @PathVariable Long subCategoryId) {

        return ResponseEntity.ok(subCategoryService.getSubCategoryById(subCategoryId));
    }

    @GetMapping("/category/{categoryId}/sub/{subCategoryId}/validate")
    public ResponseEntity<Void> validateSubCategoryExists(
            @PathVariable Long categoryId,
            @PathVariable Long subCategoryId) {

        subCategoryService.validateSubCategoryExists(categoryId, subCategoryId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{subCategoryId}")
    public ResponseEntity<SubCategoryResponse> updateSubCategoryById(
            @PathVariable Long subCategoryId,
            @Valid @RequestBody SubCategoryUpdateRequest request) {

        return ResponseEntity.ok(subCategoryService.updateSubCategoryById(subCategoryId, request));
    }

    @DeleteMapping("/{subCategoryId}")
    public ResponseEntity<Boolean> deleteSubCategoryById(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(subCategoryService.deleteSubCategoryById(subCategoryId));
    }
}
