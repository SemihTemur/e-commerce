package com.semih.common.dto.request;

import com.semih.common.constant.OutboxEventType;

import java.util.UUID;

public record ProductStockEvent(UUID eventId, Long productId, OutboxEventType eventType, Integer quantity,
                                Long createdAt) {
}
