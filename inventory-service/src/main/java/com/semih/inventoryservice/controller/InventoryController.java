package com.semih.inventoryservice.controller;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.semih.inventoryservice.config.RestApis.*;

@RestController
@RequestMapping(INVENTORY)
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping(CREATE_INVENTORY_TO_PRODUCT)
    public ResponseEntity<Void> createInventoryToProduct(@Valid @RequestBody ProductQuantityRequest productQuantityRequest){
        inventoryService.createInventoryToProduct(productQuantityRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(GET_INVENTORY_BY_PRODUCT_ID)
    public ResponseEntity<ProductStockResponse> getStockByProductId(@PathVariable Long productId){
        ProductStockResponse productStockResponse = inventoryService.getStockByProductId(productId);
        return ResponseEntity.ok(productStockResponse);
    }

    @GetMapping(CHECK_AVAILABILITY_BY_PRODUCT_ID)
    ResponseEntity<Void> checkAvailabilityByProductId(@RequestBody ProductQuantityRequest productQuantityRequest){
        inventoryService.checkAvailabilityByProductId(productQuantityRequest);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(UPDATE_INVENTORY)
    public ResponseEntity<Void> updateInventory(@Valid @RequestBody ProductQuantityRequest productQuantityRequest){
        inventoryService.updateInventory(productQuantityRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(DELETE_INVENTORY)
    public ResponseEntity<Void> deleteInventoryByProductId(@PathVariable Long productId){
        inventoryService.deleteInventoryByProductId(productId);
        return ResponseEntity.noContent().build();
    }

}
