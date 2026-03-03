package com.semih.common.dto.request;

import java.math.BigDecimal;

public record BasketItemEvent(Long productId, String productName, BigDecimal productPrice,
                              Integer quantity, BigDecimal lineTotal) {
}
