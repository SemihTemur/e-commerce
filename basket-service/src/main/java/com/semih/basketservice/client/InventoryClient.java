package com.semih.basketservice.client;

import com.semih.basketservice.config.FeignTracingConfig;
import com.semih.common.dto.request.ProductQuantityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.semih.common.config.RestApis.CHECK_AVAILABILITY;
import static com.semih.common.config.RestApis.INVENTORIES;

@FeignClient(name = "INVENTORY-SERVICE",
            path = INVENTORIES,
            configuration = FeignTracingConfig.class
)
public interface InventoryClient {
    @PostMapping(CHECK_AVAILABILITY)
    ResponseEntity<Void> checkAvailabilityByProductId(@RequestBody ProductQuantityRequest request);
}
