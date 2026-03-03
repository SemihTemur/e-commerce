package com.semih.orderservice.service;

import com.semih.common.constant.InventoryResponseStatus;
import com.semih.common.constant.OrderBasketStatus;
import com.semih.common.dto.request.*;
import com.semih.orderservice.dto.response.OrderItemResponse;
import com.semih.orderservice.dto.response.OrderResponse;
import com.semih.orderservice.entity.Order;
import com.semih.orderservice.entity.OrderItem;
import com.semih.orderservice.entity.OrderStatus;
import com.semih.orderservice.entity.ProcessedEvent;
import com.semih.orderservice.repository.OrderRepository;
import com.semih.orderservice.repository.ProcessedEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProcessedEventRepository processedEventRepository;

    private final KafkaTemplate<String,Object> kafkaTemplate;;

    private final String orderEventsTopic;

    private final String orderBasketResultTopic;

    public OrderService(OrderRepository orderRepository, ProcessedEventRepository processedEventRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        @Value("${spring.kafka.properties.topics.order-events}") String orderEventsTopic,
                        @Value("${spring.kafka.properties.topics.order-basket-result}") String orderBasketResultTopic) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
        this.orderBasketResultTopic = orderBasketResultTopic;
    }

    @Transactional
    public void createOrder(BasketEvent basketEvent){
        if(processedEventRepository.existsById(basketEvent.eventId()))
            return;

        Order order = mapToEntity(basketEvent);
        mapToOrderItem(basketEvent.basketItemEvents(),order);

        Long orderID =  orderRepository.save(order).getId();

        OrderCreatedEvent orderCreatedEvent = createOrderCreatedEvent(orderID,basketEvent.
                basketItemEvents());

        kafkaTemplate.send(orderEventsTopic,orderID.toString(),orderCreatedEvent);

        processedEventRepository.save(new ProcessedEvent(basketEvent.eventId()));
    }

    @Transactional
    public void updateOrderStatus(OrderStockResultEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + event.orderId()));

        OrderBasketStatus basketStatus;
        if (event.status() == InventoryResponseStatus.STOCK_CONFIRMED) {
            order.setOrderStatus(OrderStatus.COMPLETED);
            basketStatus = OrderBasketStatus.ORDER_COMPLETED;
        } else {
            order.setOrderStatus(OrderStatus.CANCELLED);
            basketStatus = OrderBasketStatus.ORDER_FAILED;
        }
        order.setReasonMessage(event.reason());

        // 3. Kaydetme İşlemleri
        orderRepository.save(order);
        processedEventRepository.save(new ProcessedEvent(event.eventId()));

        OrderBasketResultEvent basketEvent = new OrderBasketResultEvent(
                UUID.randomUUID(),
                order.getUserId(),
                order.getId(),
                basketStatus
        );

        kafkaTemplate.send(orderBasketResultTopic, order.getUserId(), basketEvent);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        String userId = getUserId();
        return orderRepository.findAllByUserIdWithItems(userId)
                .stream()
                .map(this::toOrderResponse) // Method reference kullanımı
                .toList();
    }

    private Order mapToEntity(BasketEvent basketEvent){
        return new Order(
                basketEvent.userId(),
                OrderStatus.PENDING,
                "Sipariş oluşturuldu, stok onayı bekleniyor.",
                basketEvent.totalAmount()
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

    private void mapToOrderItem(List<BasketItemEvent> basketItemEvents, Order order){
        for(BasketItemEvent basketItemEvent:basketItemEvents){
            OrderItem orderItem = new OrderItem(
                    basketItemEvent.productId(),
                    basketItemEvent.productName(),
                    basketItemEvent.productPrice(),
                    basketItemEvent.quantity(),
                    basketItemEvent.lineTotal()
            );

            order.addOrderItem(orderItem);
        }
    }

    private OrderCreatedEvent createOrderCreatedEvent(Long orderID, List<BasketItemEvent> basketItemList) {
        List<OrderItemEvent> orderItemEvents = basketItemList.stream()
                .map(item -> new OrderItemEvent(item.productId(), item.quantity()))
                .toList();

        return new OrderCreatedEvent(UUID.randomUUID(), orderID, orderItemEvents);
    }

    private String getUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
