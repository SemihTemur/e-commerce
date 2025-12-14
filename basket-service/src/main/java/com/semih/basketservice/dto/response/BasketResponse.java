package com.semih.basketservice.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BasketResponse(List<BasketItemResponse> items, BigDecimal basketTotal) {
}
