package com.semih.common.dto.response;

import java.util.List;

public record ProductCategoryInfoResponse(Long categoryId, String categoryName,
                                          List<SubCategoryInfoResponse> subCategoryInfoResponses) {
}
