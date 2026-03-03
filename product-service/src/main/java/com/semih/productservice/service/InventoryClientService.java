package com.semih.productservice.service;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.productservice.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InventoryClientService {

    private final InventoryClient inventoryClient;

    private static final Logger log = LoggerFactory.getLogger(InventoryClientService.class);

    public InventoryClientService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }


    @CircuitBreaker(name = "inventoryService", fallbackMethod = "checkProductAvailabilityFallback")
    public void checkProductAvailability(List<ProductQuantityRequest> requests){
        inventoryClient.checkAvailabilityByProductIds(requests);
    }

    public void checkProductAvailabilityFallback(
            List<ProductQuantityRequest> requests,
            Throwable t
    ){
        log.error("Inventory unavailable. Requests={}", requests, t);

        throw new RuntimeException("Inventory service unavailable");
    }


    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fetchStockMapInventoryServiceFallback")
    public List<ProductStockResponse> fetchProductStocks(List<Long> productIdList){
        return inventoryClient
                .getStockForProducts(productIdList)
                .getBody();
    }

    public List<ProductStockResponse> fetchProductStocksFallback(
            List<Long> productIdList,
            Throwable t
    ) {
        log.error(
                "Inventory Service unavailable while fetching product stocks. productIds={}",
                productIdList,
                t
        );

        return Collections.emptyList();
    }


}
