package com.semih.basketservice.dto.response;

import com.semih.common.dto.response.BasketProductResponse;

import java.math.BigDecimal;

public record BasketItemResponse(Long id, BasketProductResponse basketProductResponse,Integer quantity,
                                 BigDecimal lineTotal) {
}
