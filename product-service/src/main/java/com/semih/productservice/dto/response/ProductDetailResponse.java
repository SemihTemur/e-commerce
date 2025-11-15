package com.semih.productservice.dto.response;

import com.semih.common.dto.response.CategoryWithSubCategoriesResponseForProduct;
import com.semih.common.dto.response.ProductStockResponse;

import java.util.List;

public record ProductDetailResponse(ProductInfoResponse productInfoResponse,
                                    String productDescription,
                                    ProductStockResponse productStockResponse,
                                    List<CategoryWithSubCategoriesResponseForProduct> categories) {
}
