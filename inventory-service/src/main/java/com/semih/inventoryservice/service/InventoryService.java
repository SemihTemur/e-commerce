package com.semih.inventoryservice.service;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.common.exception.InsufficientStockException;
import com.semih.common.exception.InventoryException;
import com.semih.common.exception.ProductNotFoundException;
import com.semih.common.exception.StockNotFoundException;
import com.semih.inventoryservice.document.Inventory;
import com.semih.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void createInventoryToProduct(ProductQuantityRequest productQuantityRequest) {
        inventoryRepository.save(mapToInventoryEntity(productQuantityRequest));
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

        List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(productQuantityRequestMap.keySet());

        validateAllProductsExist(inventoryList, productQuantityRequestMap.keySet());

        // Stok güncelleme işini özel metoda devrediyoruz
        updateInventoryStocks(inventoryList, productQuantityRequestMap);

        inventoryRepository.saveAll(inventoryList);
    }

    // buralardakı hata mesajıda
    public void updateInventory(ProductQuantityRequest productQuantityRequest){
        Inventory inventory = inventoryRepository.findByProductId(productQuantityRequest.productId())
                .orElseThrow(()-> new StockNotFoundException("Böyle bir stok yoktur"));

        inventory.setQuantity(productQuantityRequest.quantity());

        inventoryRepository.save(inventory);
    }

    public void deleteInventoryByProductId(Long productId){
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(()-> new StockNotFoundException("Böyle bir stok yoktur"));

        inventoryRepository.delete(inventory);
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
    private Inventory mapToInventoryEntity(ProductQuantityRequest productQuantityRequest){
        return new Inventory(
                productQuantityRequest.productId(),
                productQuantityRequest.quantity()
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
