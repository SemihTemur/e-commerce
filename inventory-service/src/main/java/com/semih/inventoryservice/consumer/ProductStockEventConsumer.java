package com.semih.inventoryservice.consumer;

import com.semih.common.dto.request.ProductStockEvent;
import com.semih.common.dto.response.ProductStockResponseEvent;
import com.semih.inventoryservice.document.ProcessedEvent;
import com.semih.inventoryservice.producer.ProductStockResponseProducer;
import com.semih.inventoryservice.repository.ProcessedEventRepository;
import com.semih.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductStockEventConsumer {

    private final InventoryService inventoryService;

    private final ProductStockResponseProducer responseProducer;

    private final ProcessedEventRepository processedEventRepository;

    private static final Logger log = LoggerFactory.getLogger(ProductStockEventConsumer.class);

    public ProductStockEventConsumer(InventoryService inventoryService,
                                     ProductStockResponseProducer responseProducer,
                                     ProcessedEventRepository processedEventRepository) {
        this.inventoryService = inventoryService;
        this.responseProducer = responseProducer;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void consume(ProductStockEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        try {
            ProductStockResponseEvent response = inventoryService.executeInventoryOperation(event);

            responseProducer.send(response);

            processedEventRepository.save(new ProcessedEvent(event.eventId()));

        } catch (Exception e) {
            log.error("Event processing failed for eventId: {}, error: {}",
                    event.eventId(), e.getMessage(), e);

            throw new RuntimeException("Failed to process event: " + event.eventId(), e);
        }
    }

}
