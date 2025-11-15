package com.semih.categoryservice.dto.response;

public record CategoryWithSubCategoriesResponseForProduct(Long categoryId, String categoryName,Long subCategoryId,
                                                          String subCategoryName) {
}
