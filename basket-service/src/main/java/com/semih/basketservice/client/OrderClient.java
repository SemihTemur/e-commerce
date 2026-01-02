package com.semih.basketservice.client;

import com.semih.basketservice.config.FeignTracingConfig;
import com.semih.common.dto.request.OrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.semih.common.config.RestApis.*;

@FeignClient(name = "ORDER-SERVICE",
             path = ORDERS,
             configuration = FeignTracingConfig.class
)
public interface OrderClient {

     @PostMapping
     ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest);

}
