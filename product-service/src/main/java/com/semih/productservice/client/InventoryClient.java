package com.semih.productservice.client;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.productservice.config.FeignTracingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.semih.common.config.RestApis.*;

@FeignClient(
        name = "INVENTORY-SERVICE",
        path = INVENTORIES,
        configuration = FeignTracingConfig.class
)
public interface InventoryClient {

    @PostMapping(CREATE_TO_PRODUCT)
    ResponseEntity<Void> createInventoryToProduct(@RequestBody ProductQuantityRequest request);

    @PostMapping(STOCKS)
    ResponseEntity<List<ProductStockResponse>> getStockForProducts(@RequestBody List<Long> productIds);

    @PostMapping(VALIDATE_FOR_CHECKOUT)
     ResponseEntity<Void> checkAvailabilityByProductIds(List<ProductQuantityRequest>
                                                                      productQuantityRequests);

    @PostMapping(CHECK_AVAILABILITY)
    ResponseEntity<Void> checkAvailabilityByProductId(@RequestBody ProductQuantityRequest request);

    @PutMapping(UPDATE)
    ResponseEntity<Void> updateInventory(@RequestBody ProductQuantityRequest request);

    @DeleteMapping(DELETE_BY_PRODUCT_ID)
    ResponseEntity<Void> deleteInventoryByProductId(@PathVariable Long productId);
}

