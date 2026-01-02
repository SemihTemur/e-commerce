package com.semih.common.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(BigDecimal totalAmount, List<OrderItemRequest> orderItemRequests) {
}
