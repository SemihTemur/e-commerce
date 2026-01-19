package com.semih.inventoryservice.listener;

import com.semih.common.dto.request.ProductStockEvent;
import com.semih.common.dto.response.ProductStockResponseEvent;
import com.semih.inventoryservice.document.ProcessedEvent;
import com.semih.inventoryservice.producer.ProductStockResponseProducer;
import com.semih.inventoryservice.repository.ProcessedEventRepository;
import com.semih.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductStockEventConsumer {

    private final InventoryService inventoryService;

    private final ProductStockResponseProducer responseProducer;

    private final ProcessedEventRepository processedEventRepository;

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

        ProductStockResponseEvent productStockResponseEvent;

        productStockResponseEvent = inventoryService.executeInventoryOperation(event);
        processedEventRepository.save(new ProcessedEvent(event.eventId()));

        responseProducer.send(productStockResponseEvent);
    }

}
