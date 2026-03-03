package com.semih.basketservice.service;

import com.semih.basketservice.client.InventoryClient;
import com.semih.common.dto.request.ProductQuantityRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryClientService {

    private final InventoryClient inventoryClient;

    private static final Logger log = LoggerFactory.getLogger(InventoryClientService.class);

    public InventoryClientService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @CircuitBreaker(
            name = "inventoryService",
            fallbackMethod = "checkAvailabilityByProductIdFallback"
    )
    public void checkAvailabilityByProductId(ProductQuantityRequest request) {
        inventoryClient.checkAvailabilityByProductId(request);
    }

    public void checkAvailabilityByProductIdFallback(
            ProductQuantityRequest request,
            Throwable t
    ) {
        log.error(
                "Inventory Service unavailable while checking availability. request={}",
                request,
                t
        );
    }

}
