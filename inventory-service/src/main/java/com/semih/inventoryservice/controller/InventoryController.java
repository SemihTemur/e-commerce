package com.semih.inventoryservice.controller;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(INVENTORIES)
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping(CREATE_TO_PRODUCT)
    public ResponseEntity<Void> createInventoryToProduct(
            @Valid @RequestBody ProductQuantityRequest request) {

        inventoryService.createInventoryToProduct(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(STOCKS)
    public ResponseEntity<List<ProductStockResponse>> getStockForProducts(
            @RequestBody List<Long> productIds) {

        return ResponseEntity.ok(
                inventoryService.getStockForProducts(productIds)
        );
    }

    @PostMapping(CHECK_AVAILABILITY)
    public ResponseEntity<Void> checkAvailabilityByProductId(
            @Valid @RequestBody ProductQuantityRequest request) {

        inventoryService.checkAvailabilityByProductId(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(VALIDATE_FOR_CHECKOUT)
    public ResponseEntity<Void> checkAvailabilityByProductIds(List<ProductQuantityRequest>
                                                                          productQuantityRequests){
        inventoryService.checkAvailabilityByProductIds(productQuantityRequests);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(UPDATE)
    public ResponseEntity<Void> updateInventory(
            @Valid @RequestBody ProductQuantityRequest request) {

        inventoryService.updateInventory(request);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping(DELETE_BY_PRODUCT_ID)
    public ResponseEntity<Void> deleteInventoryByProductId(
            @PathVariable Long productId) {

        inventoryService.deleteInventoryByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
