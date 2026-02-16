package com.semih.productservice.service;

import com.semih.productservice.constant.OperationStatus;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductDelete;
import com.semih.productservice.entity.ProductUpdate;
import com.semih.productservice.repository.ProductDeleteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProductDeleteService {

    private final ProductDeleteRepository productDeleteRepository;

    public ProductDeleteService(ProductDeleteRepository productDeleteRepository) {
        this.productDeleteRepository = productDeleteRepository;
    }

    @Transactional
    public void createDeleteRequest(Product product) {
        ProductDelete delete = mapToProductDelete(product);
        productDeleteRepository.save(delete);
    }

    // ProductDeleteService
    @Transactional
    public void confirmDeletionApply(Long productId) {
        ProductDelete productDelete = productDeleteRepository
                .findPendingDeleteByProductId(productId)
                .orElseThrow(()-> new RuntimeException("bulunamadı"));

        productDelete.setOperationStatus(OperationStatus.APPROVED);

        productDeleteRepository.save(productDelete);
    }

    @Transactional
    public void rejectDeletionApply(Long productId) {
        ProductDelete delete =
                productDeleteRepository
                        .findPendingDeleteByProductId(productId)
                        .orElseThrow();

        delete.setOperationStatus(OperationStatus.REJECTED);
    }

    private ProductDelete mapToProductDelete(Product product){
        return new ProductDelete(
                product.getId(),
                OperationStatus.PENDING
        );
    }


}
