package com.semih.orderservice.service;

import com.semih.common.dto.request.OrderItemRequest;
import com.semih.common.dto.request.OrderRequest;
import com.semih.orderservice.dto.response.OrderItemResponse;
import com.semih.orderservice.dto.response.OrderResponse;
import com.semih.orderservice.entity.Order;
import com.semih.orderservice.entity.OrderItem;
import com.semih.orderservice.entity.OrderStatus;
import com.semih.orderservice.repository.OrderRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public String createOrder(OrderRequest orderRequest){
        Order order = mapToEntity(orderRequest);

        mapToOrderItem(orderRequest.orderItemRequests(),order);

        orderRepository.save(order);

        return "Succesfully";
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        String userId = getUserId();
        return orderRepository.findAllByUserIdWithItems(userId)
                .stream()
                .map(this::toOrderResponse) // Method reference kullanımı
                .toList();
    }

    private Order mapToEntity(OrderRequest orderRequest){
        return new Order(
                getUserId(),
                OrderStatus.CREATED,
                orderRequest.totalAmount()
        );
    }

    private OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getOrderItems().stream()
                        .map(this::toOrderItemResponse)
                        .toList()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getProductPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    private void mapToOrderItem(List<OrderItemRequest> orderItemRequestList,Order order){
        for(OrderItemRequest orderItemRequest:orderItemRequestList){
            OrderItem orderItem = new OrderItem(
                    orderItemRequest.productId(),
                    orderItemRequest.productName(),
                    orderItemRequest.productPrice(),
                    orderItemRequest.quantity(),
                    orderItemRequest.lineTotal()
            );

            order.addOrderItem(orderItem);
        }
    }

    private String getUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
