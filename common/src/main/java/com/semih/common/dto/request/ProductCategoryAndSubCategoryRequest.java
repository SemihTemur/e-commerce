package com.semih.common.dto.request;

import java.util.Set;

public record ProductCategoryAndSubCategoryRequest(Long categoryId, Set<Long> subCategoryIdList) {
}
