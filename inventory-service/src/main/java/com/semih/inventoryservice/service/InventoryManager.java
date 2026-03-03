package com.semih.inventoryservice.service;

import com.semih.common.dto.request.OrderCreatedEvent;
import com.semih.common.dto.request.OrderItemEvent;
import com.semih.common.exception.InsufficientStockException;
import com.semih.common.exception.StockNotFoundException;
import com.semih.inventoryservice.document.Inventory;
import com.semih.inventoryservice.document.ProcessedEvent;
import com.semih.inventoryservice.repository.InventoryRepository;
import com.semih.inventoryservice.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InventoryManager {

    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;

    public InventoryManager(InventoryRepository inventoryRepository,
                            ProcessedEventRepository processedEventRepository) {
        this.inventoryRepository = inventoryRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional // Burada hata fırlarsa Spring MongoDB'de Rollback yapar
    public void processInventoryChanges(OrderCreatedEvent orderCreatedEvent) {
        if (processedEventRepository.existsById(orderCreatedEvent.eventId())) return;

        Map<Long, Integer> productQuantityRequestMap = orderCreatedEvent.items().stream()
                .collect(Collectors.toMap(OrderItemEvent::productId, OrderItemEvent::quantity));

        List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(productQuantityRequestMap.keySet());

        // Bu metotlar hata fırlatırsa (RuntimeException) otomatik rollback olur
        validateAllProductsExist(inventoryList, productQuantityRequestMap.keySet());
        updateInventoryStocks(inventoryList, productQuantityRequestMap);

        inventoryRepository.saveAll(inventoryList);
        processedEventRepository.save(new ProcessedEvent(orderCreatedEvent.eventId()));

        // Buraya kadar geldiyse her şey başarılıdır
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

    private void updateInventoryStocks(List<Inventory> inventoryList, Map<Long, Integer> requestMap) {
        for (Inventory inventory : inventoryList) {
            Integer requestedQuantity = requestMap.get(inventory.getProductId());

            validateStockAvailability(inventory, requestedQuantity);

            int remainingStock = inventory.getQuantity() - requestedQuantity;
            inventory.setQuantity(remainingStock);
        }
    }

    private void validateStockAvailability(Inventory inventory, Integer requestedQuantity) {
        if (inventory.getQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    String.format("Yeterli stok yok. ProductId=%d, requested=%d, available=%d",
                            inventory.getProductId(), requestedQuantity, inventory.getQuantity())
            );
        }
    }
}