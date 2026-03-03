package com.semih.orderservice.consumer;

import com.semih.common.dto.request.BasketEvent;
import com.semih.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class BasketKafkaListener {

    private final OrderService orderService;

    public BasketKafkaListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "${spring.kafka.properties.topics.basket-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleBasketEvents(@Payload BasketEvent basketEvent){
        orderService.createOrder(basketEvent);
    }
}
