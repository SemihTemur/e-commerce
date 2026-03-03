package com.semih.common.dto.request;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID eventId, Long orderId,
                                List<OrderItemEvent> items) {
}
