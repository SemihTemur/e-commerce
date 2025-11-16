package com.semih.common.dto.request;

import java.util.List;

public record ProductCategoryInfoRequest(Long categoryId,List<SubCategoryInfoRequest> subCategoryInfoRequests) {
}
