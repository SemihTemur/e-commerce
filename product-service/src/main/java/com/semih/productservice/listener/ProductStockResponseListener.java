package com.semih.productservice.listener;

import com.semih.common.dto.response.ProductStockResponseEvent;
import com.semih.productservice.service.ProductService;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

@KafkaListener(
        topics = "${spring.kafka.properties.topics.product-stock-response-events}",
        groupId = "${spring.kafka.consumer.group-id}"
)
public class ProductStockResponseListener {

    private final ProductService productService;

    public ProductStockResponseListener(ProductService productService) {
        this.productService = productService;
    }

    @KafkaHandler
    public void handleProductStockResponse(@Payload ProductStockResponseEvent productStockResponseEvent){
        productService.completeProductStatus(productStockResponseEvent);
    }
}
