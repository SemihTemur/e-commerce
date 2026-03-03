package com.semih.inventoryservice.consumer;

import com.semih.common.dto.request.OrderCreatedEvent;
import com.semih.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final InventoryService inventoryService;

    public OrderEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "${spring.kafka.properties.topics.order-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleOrderEvent(@Payload OrderCreatedEvent orderCreatedEvent){
        inventoryService.checkAvailabilityByProductIds(orderCreatedEvent);
    }
}
