package com.semih.inventoryservice.producer;

import com.semih.common.dto.response.ProductStockResponseEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class ProductStockResponseProducer {

    private final KafkaTemplate<Long, ProductStockResponseEvent> kafkaTemplate;

    @Value("${spring.kafka.properties.topics.product-stock-response-events}")
    private String productStockResponseEventsTopic;

    public ProductStockResponseProducer(KafkaTemplate<Long, ProductStockResponseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ProductStockResponseEvent response) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(productStockResponseEventsTopic,
                response.productId(),
                response);
    }

}
