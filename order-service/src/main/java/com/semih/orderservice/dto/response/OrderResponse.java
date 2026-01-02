package com.semih.orderservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long orderId, String userId, String orderStatus, BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
){}
