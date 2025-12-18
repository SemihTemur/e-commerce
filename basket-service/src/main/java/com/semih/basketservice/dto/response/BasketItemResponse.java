package com.semih.basketservice.dto.response;

import com.semih.common.dto.response.BasketProductResponse;

import java.math.BigDecimal;

public record BasketItemResponse(BasketProductResponse basketProductResponse,Integer quantity,
                                 BigDecimal lineTotal) {
}
