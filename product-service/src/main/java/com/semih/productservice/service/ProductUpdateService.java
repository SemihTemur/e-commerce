package com.semih.productservice.service;

import com.semih.productservice.constant.OperationStatus;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductUpdate;
import com.semih.productservice.repository.ProductUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductUpdateService {

    private final ProductUpdateRepository productUpdateRepository;


    public ProductUpdateService(ProductUpdateRepository productUpdateRepository) {
        this.productUpdateRepository = productUpdateRepository;
    }

    @Transactional
    public void createUpdateDraft(Product product, ProductRequest request) {
        ProductUpdate update = mapToProductUpdate(product);

        if (request.productName() != null && !request.productName().isBlank()) {
            update.setName(request.productName());
        }

        if (request.productDescription() != null && !request.productDescription().isBlank()) {
            update.setProductDescription(request.productDescription());
        }

        if (request.productPrice() != null) {
            update.setPrice(request.productPrice());
        }

        update.setOperationStatus(OperationStatus.PENDING);

        productUpdateRepository.save(update);
    }

    @Transactional
    public void applyApprovedUpdate(ProductUpdate productUpdate) {
        productUpdate.setOperationStatus(OperationStatus.APPROVED);

        productUpdateRepository.save(productUpdate);
    }

    @Transactional
    public void rejectUpdateApply(Long productId) {
        ProductUpdate update =
                productUpdateRepository
                        .findPendingUpdateByProductId(productId)
                        .orElseThrow();

        update.setOperationStatus(OperationStatus.REJECTED);
    }

    public ProductUpdate getProductUpdateFindById(Long productId){
       return productUpdateRepository
                        .findPendingUpdateByProductId(productId)
                        .orElseThrow(()-> new RuntimeException("bulunamadı"));
    }

    private ProductUpdate mapToProductUpdate(Product product){
        return new ProductUpdate(
                product.getId(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                OperationStatus.PENDING
        );
    }


}
