package com.semih.basketservice.service;

import com.semih.basketservice.client.ProductClient;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.BasketProductResponse;
import com.semih.common.dto.response.ProductLineItemResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ProductClientService {

    private final ProductClient productClient;

    private static final Logger log = LoggerFactory.getLogger(ProductClientService.class);

    public ProductClientService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "getBasketProductsFallback"
    )
    public List<BasketProductResponse> getBasketProducts(List<Long> productIdList) {

        return Objects.requireNonNull(
                productClient.getBasketProductResponse(productIdList).getBody()
        );
    }

    public List<BasketProductResponse> getBasketProductsFallback(
            List<Long> productIdList,
            Throwable t
    ) {
        log.error(
                "Product Service unavailable while fetching basket products. productIds={}",
                productIdList,
                t
        );

        return Collections.emptyList();
    }

    @CircuitBreaker(
            name = "productService",
            fallbackMethod = "priceProductsForCheckoutFallback"
    )
    public List<ProductLineItemResponse> priceProductsForCheckout(
            List<ProductQuantityRequest> productQuantityRequestList
    ) {
        return Objects.requireNonNull(
                productClient
                        .priceProductsForCheckout(productQuantityRequestList)
                        .getBody()
        );
    }

    public List<ProductLineItemResponse> priceProductsForCheckoutFallback(
            List<ProductQuantityRequest> productQuantityRequestList,
            Throwable t
    ) {
        log.error(
                "Product Service unavailable while pricing products for checkout. requests={}",
                productQuantityRequestList,
                t
        );

        return Collections.emptyList();
    }


}
