package com.semih.categoryservice.service;

import com.semih.categoryservice.dto.request.CategoryRequest;
import com.semih.categoryservice.dto.response.CategoryResponse;
import com.semih.categoryservice.dto.response.CategoryWithSubCategoriesResponse;
import com.semih.categoryservice.dto.response.CategoryWithSubCategoriesResponseForProduct;
import com.semih.categoryservice.dto.response.SubCategoryResponse;
import com.semih.categoryservice.entity.Category;
import com.semih.categoryservice.entity.SubCategory;
import com.semih.categoryservice.repository.CategoryRepository;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    Category getCategoryOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(()-> new CategoryNotFoundException("Kategori bulunamadı. ID: " + categoryId));
    }

    //Save
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = mapToCategoryCreate(categoryRequest);
        return mapToCategoryResponse(categoryRepository.save(category));
    }

    //List
    public List<CategoryResponse> getCategoryList() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryWithSubCategoriesResponse getCategoryWithSubCategoriesById(Long categoryId){
        Category category = getCategoryOrThrow(categoryId);
        return mapToCategoryWithSubCategoriesResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryWithSubCategoriesResponseForProduct getCategoryWithSubCategoriesForProductList(Long categoryId, Long requestSubCategoryId)  {
        Category category = getCategoryOrThrow(categoryId);

        for (SubCategory subCategory : category.getSubCategory()) {
            if (subCategory.getId().equals(requestSubCategoryId))
                return new CategoryWithSubCategoriesResponseForProduct(category.getId(),category.getCategoryName(),
                        subCategory.getId(),subCategory.getSubCategoryName());;
        }

        return null;
    }

    @Transactional(readOnly = true)
    public List<CategoryWithSubCategoriesResponse> getAllCategoryWithSubCategories(){
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryWithSubCategoriesResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void existsCategoryWithSubCategories(CategoryValidationRequest categoryValidationRequest){
        Category category = getCategoryOrThrow(categoryValidationRequest.categoryId());
        validateSubCategories(category,categoryValidationRequest.subCategoriesId());
    }

    @Transactional(readOnly = true)
    public void validateCategoryHierarchy(List<CategoryValidationRequest> categoryValidationRequestList){
        for(CategoryValidationRequest categoryValidationRequest:categoryValidationRequestList){
            Category category = getCategoryOrThrow(categoryValidationRequest.categoryId());
            validateSubCategories(category,categoryValidationRequest.subCategoriesId());
        }
    }

    //Put
    public CategoryResponse updateCategoryById(Long categoryId, CategoryRequest categoryRequest) {
        Category updatedCategory = mapToCategoryUpdate(categoryId,categoryRequest);
        return mapToCategoryResponse(categoryRepository.save(updatedCategory));
    }

    //Delete
    public Boolean deleteCategoryById(Long categoryId){
        Category deletedCategory = getCategoryOrThrow(categoryId);
        categoryRepository.delete(deletedCategory);
        return true;
    }

    //toResponse
    private CategoryWithSubCategoriesResponse mapToCategoryWithSubCategoriesResponse(Category category) {
        List<SubCategoryResponse> subCategoryResponseList = new ArrayList<>();
        for(SubCategory subCategory:category.getSubCategory()){
            SubCategoryResponse subCategoryResponse = new SubCategoryResponse(subCategory.getId(),
                    subCategory.getSubCategoryName(),subCategory.getCreatedAt(),subCategory.getUpdatedAt());
            subCategoryResponseList.add(subCategoryResponse);
        }
        return new CategoryWithSubCategoriesResponse(category.getId(),category.getCategoryName()
                ,subCategoryResponseList,category.getCreatedAt(),category.getUpdatedAt()
        );
    }


    private CategoryResponse mapToCategoryResponse(Category category){
        return new CategoryResponse(category.getId(), category.getCategoryName(), category.getCreatedAt(),
                category.getUpdatedAt());
    }

    //toEntity
    private Category mapToCategoryUpdate(Long categoryId, CategoryRequest categoryRequest){
        Category updatedCategory = getCategoryOrThrow(categoryId);
        updatedCategory.setCategoryName(categoryRequest.categoryName());
        return updatedCategory;
    }

    private void validateSubCategories(Category category, List<Long> subCategoriesId){
        Set<Long> existingIds  = category.getSubCategory().stream().map(SubCategory::getId)
                .collect(Collectors.toSet());;

        for(Long subCategoryId:subCategoriesId){
            if(!existingIds.contains(subCategoryId))
                throw new SubCategoryNotFoundException("Alt kategori bulunamadı. ID: " + subCategoryId);
        }
    }

    private Category mapToCategoryCreate(CategoryRequest categoryRequest){
        return new Category(categoryRequest.categoryName());
    }

}
