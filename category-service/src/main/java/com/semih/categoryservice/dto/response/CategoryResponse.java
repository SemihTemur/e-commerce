package com.semih.categoryservice.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(Long id, String categoryName,LocalDateTime createdAt,LocalDateTime updatedAt) {
}
