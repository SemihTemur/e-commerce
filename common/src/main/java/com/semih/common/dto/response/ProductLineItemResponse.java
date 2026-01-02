package com.semih.common.dto.response;

import java.math.BigDecimal;

public record ProductLineItemResponse(Long productId, String productName, BigDecimal unitPrice,
                                      Integer quantity) {
}
