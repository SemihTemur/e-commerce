package com.semih.common.dto.request;

import com.semih.common.constant.InventoryResponseStatus;

import java.util.UUID;

public record OrderStockResultEvent(UUID eventId, Long orderId,
                                    InventoryResponseStatus status,
                                    String reason) {
}
