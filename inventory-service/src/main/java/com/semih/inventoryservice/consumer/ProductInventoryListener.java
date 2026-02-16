package com.semih.inventoryservice.consumer;

import com.semih.common.dto.request.ProductStockEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
public class ProductInventoryListener {

    private final ProductStockEventConsumer productStockEventConsumer;

    public ProductInventoryListener(ProductStockEventConsumer productStockEventConsumer) {
        this.productStockEventConsumer = productStockEventConsumer;
    }

    @KafkaListener(
            topics = "${spring.kafka.properties.topics.product-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleProductStock(@Payload ProductStockEvent productStockEvent){
        productStockEventConsumer.consume(productStockEvent);
    }

}
