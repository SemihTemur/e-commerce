package com.semih.productservice.consumer;

import com.semih.common.dto.response.ProductStockResponseEvent;
import com.semih.productservice.service.ProductManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ProductStockResponseListener {

    private final ProductManager productManager;
    private static final Logger logger = LoggerFactory.getLogger(ProductStockResponseListener.class);

    public ProductStockResponseListener(ProductManager productManager) {
        this.productManager = productManager;
    }

    @KafkaListener(
            topics = "${spring.kafka.properties.topics.product-stock-response-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleProductStockResponse(@Payload ProductStockResponseEvent productStockResponseEvent){
        logger.info("Listening:{}", productStockResponseEvent);
        productManager.completeProductStatus(productStockResponseEvent);
    }
}
