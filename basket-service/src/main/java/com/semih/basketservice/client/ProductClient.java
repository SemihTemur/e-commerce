package com.semih.basketservice.client;

import com.semih.basketservice.config.FeignTracingConfig;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.BasketProductResponse;
import com.semih.common.dto.response.ProductLineItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static com.semih.common.config.RestApis.*;

@FeignClient(
        name = "PRODUCT-SERVICE",
        path = PRODUCTS, // ortak base path
        configuration = FeignTracingConfig.class
)
public interface ProductClient {
    @GetMapping(BASKET_PRODUCT)
    ResponseEntity<List<BasketProductResponse>> getBasketProductResponse(@RequestBody
                                                                         List<Long> productIdList);
    @PostMapping(CHECKOUT_PRICE)
     ResponseEntity<List<ProductLineItemResponse>> priceProductsForCheckout(
             List<ProductQuantityRequest> productQuantityRequests);
}

