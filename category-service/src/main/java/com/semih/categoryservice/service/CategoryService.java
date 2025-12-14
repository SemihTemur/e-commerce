package com.semih.categoryservice.service;

import com.semih.categoryservice.dto.request.CategoryRequest;
import com.semih.categoryservice.dto.response.CategoryResponse;
import com.semih.categoryservice.dto.response.CategoryWithSubCategoriesResponse;
import com.semih.categoryservice.dto.response.SubCategoryResponse;
import com.semih.categoryservice.entity.Category;
import com.semih.categoryservice.entity.SubCategory;
import com.semih.categoryservice.repository.CategoryRepository;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.request.SubCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import com.semih.common.dto.response.SubCategoryInfoResponse;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        Category category = mapToCategoryEntity(categoryRequest);
        return mapToCategoryResponse(categoryRepository.save(category));
    }

    //List
    public List<CategoryResponse> getCategoryList() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    public void validateCategoryExistsById(Long categoryId){
        if(!categoryRepository.existsById(categoryId)){
            throw new CategoryNotFoundException("Kategori bulunamadı. ID: " + categoryId);
        }

    }

    @Transactional(readOnly = true)
    public CategoryWithSubCategoriesResponse getCategoryWithSubCategoriesById(Long categoryId){
        Category category = getCategoryOrThrow(categoryId);
        return mapToCategoryWithSubCategoriesResponse(category);
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryInfoResponse> getCategoryWithSubCategoriesForProductList(
            List<ProductCategoryInfoRequest> productCategoryInfoRequests)  {

        List<ProductCategoryInfoResponse> productCategoryResponses = new ArrayList<>();

        for(ProductCategoryInfoRequest productCategoryInfoRequest:productCategoryInfoRequests){
            Category category = getCategoryOrThrow(productCategoryInfoRequest.categoryId());

            Set<Long> subCategoryRequestIdList = productCategoryInfoRequest.subCategoryInfoRequests().stream()
                    .map(SubCategoryInfoRequest::subCategoryId)
                    .collect(Collectors.toSet());

            List<SubCategoryInfoResponse>  subCategoryInfoResponseList = getSubCategoryInfoResponseList(category,
                    subCategoryRequestIdList);

            productCategoryResponses.add(new ProductCategoryInfoResponse(category.getId(),category.getCategoryName()
                    ,subCategoryInfoResponseList));

        }

        return productCategoryResponses;
    }

    private List<SubCategoryInfoResponse> getSubCategoryInfoResponseList(Category category,
                                                                         Set<Long> subCategoryRequestIdList){
        List<SubCategoryInfoResponse> subCategoryInfoResponseList = new ArrayList<>();

        Map<Long, SubCategory> subCategoryMap = category.getSubCategory().stream()
                .collect(Collectors.toMap(SubCategory::getId, sc -> sc));

        for(Long requestedId : subCategoryRequestIdList){
            SubCategory subCategory = subCategoryMap.get(requestedId);
            if(subCategory == null){
                throw new SubCategoryNotFoundException("Alt kategori bulunamadı. ID: " + requestedId);
            }
            subCategoryInfoResponseList.add(new SubCategoryInfoResponse(subCategory.getId(),
                    subCategory.getSubCategoryName()));
        }

        return subCategoryInfoResponseList;
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

    private Category mapToCategoryEntity(CategoryRequest categoryRequest){
        return new Category(categoryRequest.categoryName());
    }

}
