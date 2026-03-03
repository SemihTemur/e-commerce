package com.semih.orderservice.consumer;

import com.semih.common.dto.request.OrderStockResultEvent;
import com.semih.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class InventoryKafkaListener {

    private final OrderService orderService;

    public InventoryKafkaListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "${spring.kafka.properties.topics.inventory-response}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleInventoryResponseEvents(@Payload OrderStockResultEvent event) {
        // OrderService içindeki işleme mantığına yönlendiriyoruz
        orderService.updateOrderStatus(event);
    }
}
