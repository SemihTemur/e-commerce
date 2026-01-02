package com.semih.orderservice.controller;

import com.semih.common.dto.request.OrderRequest;
import com.semih.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(ORDERS)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest){
       String message = orderService.createOrder(orderRequest);
       return ResponseEntity.ok(message);
    }

}
