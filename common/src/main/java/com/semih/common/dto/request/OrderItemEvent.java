package com.semih.common.dto.request;

public record OrderItemEvent(Long productId,
                             Integer quantity) {
}
