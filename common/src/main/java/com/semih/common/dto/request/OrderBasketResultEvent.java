package com.semih.common.dto.request;

import com.semih.common.constant.OrderBasketStatus;

import java.util.UUID;

public record OrderBasketResultEvent(UUID eventId,
                                     String userId,
                                     Long orderId,
                                     OrderBasketStatus status) {
}
