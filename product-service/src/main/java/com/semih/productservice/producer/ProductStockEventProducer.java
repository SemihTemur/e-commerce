package com.semih.productservice.producer;

import com.semih.common.dto.request.ProductStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductStockEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductStockEventProducer.class);

    private final KafkaTemplate<Long, ProductStockEvent> kafkaTemplate;

    @Value("${spring.kafka.properties.topics.product-events}")
    private String productEventsTopic;

    public ProductStockEventProducer(
            KafkaTemplate<Long, ProductStockEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ProductStockEvent event, Long key) {
        try {
            kafkaTemplate.send(productEventsTopic, key, event).get();

            log.info(
                    "ProductStockEvent sent to Kafka | productId={} | eventId={}",
                    key,
                    event.eventId()
            );

        } catch (Exception e) {
            throw new RuntimeException("Kafka message send failed!", e);
        }
    }
}
