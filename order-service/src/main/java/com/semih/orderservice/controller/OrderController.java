package com.semih.orderservice.controller;

import com.semih.common.dto.request.OrderRequest;
import com.semih.orderservice.dto.response.OrderResponse;
import com.semih.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(){
        List<OrderResponse> orderResponseList = orderService.getOrders();
        return ResponseEntity.ok(orderResponseList);
    }

}
