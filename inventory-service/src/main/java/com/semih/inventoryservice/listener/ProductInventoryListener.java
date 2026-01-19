package com.semih.inventoryservice.listener;

import com.semih.common.dto.request.ProductStockEvent;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

@KafkaListener(
        topics = "${spring.kafka.properties.topics.product-events}",
        groupId = "${spring.kafka.consumer.group-id}"
)
public class ProductInventoryListener {

    private final ProductStockEventConsumer productStockEventConsumer;

    public ProductInventoryListener(ProductStockEventConsumer productStockEventConsumer) {
        this.productStockEventConsumer = productStockEventConsumer;
    }

    @KafkaHandler
    public void handleProductStock(@Payload ProductStockEvent productStockEvent){
        productStockEventConsumer.consume(productStockEvent);
    }

}
