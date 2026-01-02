package com.semih.productservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductInfoResponse(Long productId, String productName, BigDecimal productPrice,
                                LocalDateTime createdAt,LocalDateTime updatedAt) {
}
