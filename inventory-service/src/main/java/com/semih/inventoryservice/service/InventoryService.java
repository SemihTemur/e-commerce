package com.semih.inventoryservice.service;

import com.semih.common.constant.EntityStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }


    public ProductStockResponseEvent executeInventoryOperation(ProductStockEvent event) {
        return switch (event.eventType()) {
            case CREATED -> {
                createInventory(event);
                yield createSuccessResponse(event, EntityStatus.ACTIVE,
                        "Product inventory has been created successfully.");
            }
            case UPDATED -> {
                updateInventory(event);
                // Güncelleme bittiği için tekrar ACTIVE olmalı
                yield createSuccessResponse(event, EntityStatus.ACTIVE,
                        "Product stock has been updated successfully.");
            }
            case DELETED -> {
                deleteInventory(event.productId());
                yield createSuccessResponse(event, EntityStatus.DELETING,
                        "Product inventory has been deleted successfully.");
            }
        };
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

    @Transactional
    public void checkAvailabilityByProductIds(List<ProductQuantityRequest> productQuantityRequests) {
        Map<Long, Integer> productQuantityRequestMap = productQuantityRequests.stream()
                .collect(Collectors.toMap(ProductQuantityRequest::productId,
                        ProductQuantityRequest::quantity));

        List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(
                productQuantityRequestMap.keySet());

        validateAllProductsExist(inventoryList, productQuantityRequestMap.keySet());

        // Stok güncelleme işini özel metoda devrediyoruz
        updateInventoryStocks(inventoryList, productQuantityRequestMap);

        inventoryRepository.saveAll(inventoryList);
    }

    public void updateInventory(ProductStockEvent event) {
        Inventory inventory = inventoryRepository.findByProductId(event.productId())
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        inventory.setQuantity(event.quantity());
        inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));

        inventoryRepository.delete(inventory);
    }

    private ProductStockResponseEvent createSuccessResponse(ProductStockEvent event, EntityStatus status,
                                                            String message) {
        return new ProductStockResponseEvent(
                event.eventId(),
                event.productId(),
                status,
                message
        );
    }

    //toResponse
    private List<ProductStockResponse> mapToProductStockResponse(List<Inventory> inventorySet){
        List<ProductStockResponse> productStockResponses = new ArrayList<>();

        for(Inventory inventory:inventorySet)
            productStockResponses.add(new ProductStockResponse(inventory.getProductId(),
                    inventory.getQuantity()));

        return productStockResponses;
    }

    //toEntity
    private Inventory mapToInventoryEntity(ProductStockEvent productStockEvent){
        return new Inventory(
                productStockEvent.productId(),
                productStockEvent.quantity()
        );
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
