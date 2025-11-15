package com.semih.categoryservice.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryWithSubCategoriesResponse(Long id, String categoryName,
                                                List<SubCategoryResponse> subCategoryResponseList,
                                                LocalDateTime createdAt, LocalDateTime updatedAt) {
}
