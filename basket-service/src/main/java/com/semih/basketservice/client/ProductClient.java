package com.semih.basketservice.client;

import com.semih.basketservice.config.FeignTracingConfig;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.BasketProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.semih.basketservice.config.RestApis.*;

@FeignClient(name = "PRODUCT-SERVICE",
        path = PRODUCT,
        configuration = FeignTracingConfig.class)
public interface ProductClient {

    @PostMapping(CHECK_AVAILABILITY_BY_PRODUCT_ID)
    ResponseEntity<Void> checkAvailabilityByProductId(@RequestBody ProductQuantityRequest productQuantityRequest);

    @GetMapping(GET_BASKET_PRODUCT_BY_ID)
    ResponseEntity<BasketProductResponse> getBasketProductResponse(@PathVariable Long productId);
}
