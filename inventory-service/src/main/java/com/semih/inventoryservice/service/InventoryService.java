package com.semih.inventoryservice.service;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.common.exception.InventoryException;
import com.semih.common.exception.ProductNotFoundException;
import com.semih.common.exception.StockNotFoundException;
import com.semih.inventoryservice.document.Inventory;
import com.semih.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void createInventoryToProduct(ProductQuantityRequest productQuantityRequest) {
        inventoryRepository.save(mapToInventoryEntity(productQuantityRequest));
    }

    public ProductStockResponse getStockByProductId(Long productId){
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(()-> new StockNotFoundException("Böyle bir stok yoktur"));
        return mapToProductStockResponse(inventory);
    }

    public void checkAvailabilityByProductId(ProductQuantityRequest productQuantityRequest){
        Inventory inventory = inventoryRepository.findByProductId(productQuantityRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException("Ürün bulunamadı. " +
                        "Lütfen ürün bilgilerini kontrol edin ve tekrar deneyin."));

        if(inventory.getQuantity()<productQuantityRequest.quantity())
            throw new InventoryException("Mevcut stok miktarınız talep ettiğiniz miktarı karşılamamaktadır. " +
                    "Lütfen daha az miktar girin.");

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
    private ProductStockResponse mapToProductStockResponse(Inventory inventory){
        return new ProductStockResponse(inventory.getQuantity());
    }

    //toEntity
    private Inventory mapToInventoryEntity(ProductQuantityRequest productQuantityRequest){
        return new Inventory(
                productQuantityRequest.productId(),
                productQuantityRequest.quantity()
        );
    }


}
