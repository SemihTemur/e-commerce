package com.semih.productservice.client;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.productservice.config.FeignTracingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.semih.productservice.config.RestApis.*;

@FeignClient(name = "INVENTORY-SERVICE",
        path = INVENTORY,
        configuration = FeignTracingConfig.class)
public interface InventoryClient {

    @PostMapping(INVENTORY+CREATE_INVENTORY_TO_PRODUCT)
    ResponseEntity<Void> createInventoryToProduct(@RequestBody ProductQuantityRequest productQuantityRequest);

    @GetMapping(GET_INVENTORY_BY_PRODUCT_ID+"/{productId}")
    ResponseEntity<ProductStockResponse> getStockByProductId(@PathVariable Long productId);

    @PostMapping(CHECK_AVAILABILITY_BY_PRODUCT_ID)
    ResponseEntity<Void> checkAvailabilityByProductId(@RequestBody ProductQuantityRequest productQuantityRequest);

    @PutMapping(UPDATE_INVENTORY)
    ResponseEntity<Void> updateInventory(@RequestBody ProductQuantityRequest productQuantityRequest);

    @DeleteMapping(DELETE_INVENTORY+"/{productId}")
    ResponseEntity<Void> deleteInventoryByProductId(@PathVariable Long productId);


}
