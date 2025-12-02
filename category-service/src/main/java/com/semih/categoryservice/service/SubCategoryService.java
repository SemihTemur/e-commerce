package com.semih.categoryservice.service;

import com.semih.categoryservice.dto.request.SubCategoryCreateRequest;
import com.semih.categoryservice.dto.request.SubCategoryUpdateRequest;
import com.semih.categoryservice.dto.response.SubCategoryResponse;
import com.semih.categoryservice.entity.SubCategory;
import com.semih.categoryservice.repository.SubCategoryRepository;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryService categoryService;

    public SubCategoryService(SubCategoryRepository subCategoryRepository, CategoryService categoryService) {
        this.subCategoryRepository = subCategoryRepository;
        this.categoryService = categoryService;
    }

    //Post
    public SubCategoryResponse createSubCategory(SubCategoryCreateRequest subCategoryCreateRequest){
         SubCategory savedDubCategory = subCategoryRepository.save(mapToSubCategoryCreate(subCategoryCreateRequest));
         return mapToSubCategoryResponse(savedDubCategory);
    }

    //List
    public List<SubCategoryResponse> getSubCategoryList() {
        return subCategoryRepository.findAll().stream()
                .map(this::mapToSubCategoryResponse)
                .collect(Collectors.toList());
    }

    public SubCategoryResponse getSubCategoryById(Long id){
        return mapToSubCategoryResponse(getSubCategoryOrThrow(id));
    }

    public void validateSubCategoryExists(Long categoryId,Long subCategoryId){
        SubCategory subCategory = getSubCategoryOrThrow(subCategoryId);

        if(!subCategory.getCategory().getId().equals(categoryId)){
            throw new CategoryNotFoundException("Kategori bulunamadı. ID: " + categoryId);
        }
    }

    //Put
    public SubCategoryResponse updateSubCategoryById(Long id, SubCategoryUpdateRequest subCategoryUpdateRequest){
        SubCategory updatedSubCategory = subCategoryRepository.save(mapToSubCategoryUpdate(id,subCategoryUpdateRequest));
        return mapToSubCategoryResponse(updatedSubCategory);
    }

    //Delete
    public Boolean deleteSubCategoryById(Long subCategoryId){
        SubCategory deletedSubCategory = getSubCategoryOrThrow(subCategoryId);
        subCategoryRepository.delete(deletedSubCategory);
        return true;
    }

    //toResponse
    private SubCategoryResponse mapToSubCategoryResponse(SubCategory subCategory){
        return new SubCategoryResponse(subCategory.getId(),subCategory.getSubCategoryName(),
                subCategory.getCreatedAt(),subCategory.getUpdatedAt());
    }

    //toEntity
    private SubCategory mapToSubCategoryUpdate(Long id,SubCategoryUpdateRequest subCategoryUpdateRequest){
        SubCategory updatedSubCategory = getSubCategoryOrThrow(id);
        updatedSubCategory.setSubCategoryName(subCategoryUpdateRequest.subCategoryName());
        return updatedSubCategory;
    }

    private SubCategory mapToSubCategoryCreate(SubCategoryCreateRequest subCategoryCreateRequest){
        return new SubCategory(subCategoryCreateRequest.subCategoryName(),
                categoryService.getCategoryOrThrow(subCategoryCreateRequest.categoryId()));
    }

    private SubCategory getSubCategoryOrThrow(Long id){
        return subCategoryRepository.findById(id)
                .orElseThrow(()-> new SubCategoryNotFoundException("Alt Kategori Bulunamadı "+id));
    }

}
