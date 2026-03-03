package com.semih.inventoryservice.service;

import com.semih.common.constant.EntityStatus;
import com.semih.common.constant.InventoryResponseStatus;
import com.semih.common.constant.OutboxEventType;
import com.semih.common.dto.request.OrderCreatedEvent;
import com.semih.common.dto.request.OrderStockResultEvent;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.request.ProductStockEvent;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.common.dto.response.ProductStockResponseEvent;
import com.semih.common.exception.InsufficientStockException;
import com.semih.common.exception.InventoryException;
import com.semih.common.exception.ProductNotFoundException;
import com.semih.common.exception.StockNotFoundException;
import com.semih.inventoryservice.document.Inventory;
import com.semih.inventoryservice.repository.InventoryRepository;
import com.semih.inventoryservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final InventoryManager inventoryManager;

    private final KafkaTemplate<String, Object> orderStockResultEventKafkaTemplate;

    private final String inventoryResponseTopic;

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryManager inventoryManager,
                            KafkaTemplate<String, Object> orderStockResultEventKafkaTemplate,
                            @Value("${spring.kafka.properties.topics.inventory-response}") String inventoryResponseTopic) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryManager = inventoryManager;
        this.orderStockResultEventKafkaTemplate = orderStockResultEventKafkaTemplate;
        this.inventoryResponseTopic = inventoryResponseTopic;
    }

    @Transactional
    public ProductStockResponseEvent executeInventoryOperation(ProductStockEvent event) {
        try {
            return switch (event.eventType()) {
                case CREATED -> {
                    createInventory(event);
                    yield createResponse(event, EntityStatus.ACTIVE, event.eventType(),
                            "Product inventory has been created successfully.");
                }
                case UPDATED -> {
                    updateInventory(event);
                    yield createResponse(event, EntityStatus.ACTIVE,event.eventType(),
                            "Product stock has been updated successfully.");
                }
                case DELETED -> {
                    deleteInventory(event.productId());
                    yield createResponse(event, EntityStatus.ACTIVE,event.eventType(),
                            "Product inventory has been deleted successfully.");
                }
            };
        } catch (Exception ex) {
            log.error("Inventory operation failed for product: {} | Error: {}",
                    event.productId(), ex.getMessage());

            // İşte burada REJECTED dönüyoruz ki Product Service ne olduğunu anlasın
            return createResponse(
                    event,
                    EntityStatus.REJECTED,event.eventType(),
                    "Inventory Error: " + ex.getMessage()
            );
        }
    }

    public void createInventory(ProductStockEvent event) {
        inventoryRepository.save(new Inventory(
                event.productId(),
                event.quantity()
        ));
    }

    public List<ProductStockResponse> getStockForProducts(List<Long> productIdList){
        List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(new HashSet<>(productIdList));

        Set<Long> requestProductIds = new HashSet<>(productIdList);

        validateAllProductsExist(inventoryList,requestProductIds);

        return mapToProductStockResponse(inventoryList);
    }

    public void checkAvailabilityByProductId(ProductQuantityRequest productQuantityRequest){
        Inventory inventory = inventoryRepository.findByProductId(productQuantityRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException("Ürün bulunamadı. " +
                        "Lütfen ürün bilgilerini kontrol edin ve tekrar deneyin."));

        if(inventory.getQuantity()<productQuantityRequest.quantity())
            throw new InventoryException("Mevcut stok miktarınız talep ettiğiniz miktarı karşılamamaktadır. " +
                    "Lütfen daha az miktar girin.");
    }

    public void checkAvailabilityByProductIds(OrderCreatedEvent orderCreatedEvent) {
        try {
            // Transactional işlemi başlat
            inventoryManager.processInventoryChanges(orderCreatedEvent);

            // Başarılıysa onay eventi gönder
            sendResponseToOrder(orderCreatedEvent.orderId(),
                    InventoryResponseStatus.STOCK_CONFIRMED, "Stok rezerve edildi.");

        } catch (Exception e) {

            // Hata durumunda RED eventi gönderiyoruz
            sendResponseToOrder(orderCreatedEvent.orderId(),
                    InventoryResponseStatus.STOCK_REJECTED, e.getMessage());

            // Loglama yapabilirsin
            log.error("Transaction iptal edildi ve Red mesajı gönderildi: " + e.getMessage());
        }
    }

    @Transactional
    public void updateInventory(ProductStockEvent event) {
        Inventory inventory = inventoryRepository.findByProductId(event.productId())
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        inventory.setQuantity(event.quantity());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void deleteInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        inventoryRepository.delete(inventory);
    }

    private ProductStockResponseEvent createResponse(ProductStockEvent event, EntityStatus status,
                                                     OutboxEventType outboxEventType, String message) {
        return new ProductStockResponseEvent(
                event.eventId(),
                event.productId(),
                status,
                outboxEventType,
                message
        );
    }

    private void sendResponseToOrder(Long orderId, InventoryResponseStatus status, String reason) {
        OrderStockResultEvent response = new OrderStockResultEvent(UUID.randomUUID(),orderId, status, reason);
        orderStockResultEventKafkaTemplate.send(inventoryResponseTopic, orderId.toString(), response);
    }

    //toResponse
    private List<ProductStockResponse> mapToProductStockResponse(List<Inventory> inventorySet){
        List<ProductStockResponse> productStockResponses = new ArrayList<>();

        for(Inventory inventory:inventorySet)
            productStockResponses.add(new ProductStockResponse(inventory.getProductId(),
                    inventory.getQuantity()));

        return productStockResponses;
    }

    private void validateAllProductsExist(List<Inventory> inventoryList, Set<Long> requestProductIds) {
        Set<Long> inventoryProductIds = inventoryList.stream()
                .map(Inventory::getProductId)
                .collect(Collectors.toSet());

        for (Long reqId : requestProductIds) {
            if (!inventoryProductIds.contains(reqId)) {
                throw new StockNotFoundException("Stok bulunamadı. productId=" + reqId);
            }
        }
    }



}
