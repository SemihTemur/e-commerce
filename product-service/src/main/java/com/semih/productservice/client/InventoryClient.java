package com.semih.productservice.client;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.semih.productservice.config.RestApis.*;

@FeignClient(name = "inventory",url = "http://localhost:8084/dev/v1/inventory")
public interface InventoryClient {

    @PostMapping(CREATE_INVENTORY_TO_PRODUCT)
    ResponseEntity<Void> createInventoryToProduct(@RequestBody ProductQuantityRequest productQuantityRequest);

    @GetMapping(GET_INVENTORY_BY_PRODUCT_ID+"/{productId}")
    ResponseEntity<ProductStockResponse> getStockByProductId(@PathVariable Long productId);

    @PutMapping(UPDATE_INVENTORY)
    ResponseEntity<Void> updateInventory(@RequestBody ProductQuantityRequest productQuantityRequest);

    @DeleteMapping(DELETE_INVENTORY+"/{productId}")
    ResponseEntity<Void> deleteInventoryByProductId(@PathVariable Long productId);


}
