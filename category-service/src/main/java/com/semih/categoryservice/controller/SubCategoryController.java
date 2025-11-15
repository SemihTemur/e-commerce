package com.semih.categoryservice.controller;

import com.semih.categoryservice.dto.request.SubCategoryCreateRequest;
import com.semih.categoryservice.dto.request.SubCategoryUpdateRequest;
import com.semih.categoryservice.dto.response.SubCategoryResponse;
import com.semih.categoryservice.service.SubCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.categoryservice.config.RestApis.*;

@RestController
@RequestMapping(SUB_CATEGORY)
public class SubCategoryController {
    private final SubCategoryService subCategoryService;

    public SubCategoryController(SubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }

    @PostMapping(CREATE_SUBCATEGORY)
    public ResponseEntity<SubCategoryResponse> createSubCategory(@Valid @RequestBody SubCategoryCreateRequest subCategoryCreateRequest){
        SubCategoryResponse subCategoryResponse = subCategoryService.createSubCategory(subCategoryCreateRequest);
        return ResponseEntity.ok(subCategoryResponse);
    }

    @GetMapping(GET_SUBCATEGORY_LIST)
    public ResponseEntity<List<SubCategoryResponse>> getSubCategoryList(){
        List<SubCategoryResponse> subCategoryResponseList = subCategoryService.getSubCategoryList();
        return ResponseEntity.ok(subCategoryResponseList);
    }

    @GetMapping(GET_SUBCATEGORY_BY_ID+"/{subCategoryId}")
    public ResponseEntity<SubCategoryResponse> getSubCategoryById(@PathVariable Long subCategoryId){
        SubCategoryResponse subCategoryResponse = subCategoryService.getSubCategoryById(subCategoryId);
        return ResponseEntity.ok(subCategoryResponse);
    }

    @PutMapping(UPDATE_SUBCATEGORY+"/{subCategoryId}")
    public ResponseEntity<SubCategoryResponse> updateSubCategoryById(@PathVariable Long subCategoryId,@Valid @RequestBody SubCategoryUpdateRequest subCategoryUpdateRequest){
        SubCategoryResponse subCategoryResponse = subCategoryService.updateSubCategoryById(subCategoryId,subCategoryUpdateRequest);
        return ResponseEntity.ok(subCategoryResponse);
    }

    @DeleteMapping(DELETE_SUBCATEGORY+"/{subCategoryId}")
    public ResponseEntity<Boolean> deleteSubCategoryById(@PathVariable Long subCategoryId){
        return ResponseEntity.ok(subCategoryService.deleteSubCategoryById(subCategoryId));
    }
}
