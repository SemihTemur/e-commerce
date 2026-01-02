package com.semih.common.dto.request;

import java.math.BigDecimal;

public record OrderItemRequest(Long productId,String productName,BigDecimal productPrice,
                               Integer quantity,BigDecimal lineTotal) {
}
