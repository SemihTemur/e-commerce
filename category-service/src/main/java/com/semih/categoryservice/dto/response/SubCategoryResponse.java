package com.semih.categoryservice.dto.response;

import java.time.LocalDateTime;

public record SubCategoryResponse(Long id, String subCategoryName, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
