package com.semih.common.dto.response;

import java.math.BigDecimal;

public record BasketProductResponse(Long productId, String productName, BigDecimal productPrice,
                                    BasketQuantityResponse basketQuantityResponse) {
}
