package com.semih.common.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BasketEvent(UUID eventId, String userId,BigDecimal totalAmount, List<BasketItemEvent> basketItemEvents) {
}
