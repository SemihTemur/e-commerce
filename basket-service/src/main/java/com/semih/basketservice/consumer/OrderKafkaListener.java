package com.semih.basketservice.consumer;

import com.semih.basketservice.service.BasketManager;
import com.semih.common.dto.request.OrderBasketResultEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaListener {

    private final BasketManager basketManager;

    public OrderKafkaListener(BasketManager basketManager) {
        this.basketManager = basketManager;
    }

    @KafkaListener(
            topics = "${spring.kafka.properties.topics.order-basket-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleOrderResult(@Payload OrderBasketResultEvent orderBasketResultEvent){
        basketManager.handleOrderResult(orderBasketResultEvent);
    }
}
