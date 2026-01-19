package com.semih.common.dto.response;

import com.semih.common.constant.EntityStatus;
import com.semih.common.constant.OutboxEventType;

import java.util.UUID;

public record ProductStockResponseEvent(UUID eventId, Long productId, EntityStatus operation,
                                        String message){
}
