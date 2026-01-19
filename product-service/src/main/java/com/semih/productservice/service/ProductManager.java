package com.semih.productservice.service;

import com.semih.common.constant.EntityStatus;
import com.semih.common.constant.OutboxEventType;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductCategoryMapping;
import com.semih.productservice.exception.NotFoundException;
import com.semih.productservice.exception.ProductNotFoundException;
import com.semih.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductManager {

    private final ProductRepository productRepository;

    private final OutboxService outboxService;

    public ProductManager(ProductRepository productRepository, OutboxService outboxService) {
        this.productRepository = productRepository;
        this.outboxService = outboxService;
    }

    // create
    @Transactional
    public void persistProductAndOutbox(Product savedProduct,Integer quantity) {
        // Ürünü kaydet
        productRepository.save(savedProduct);

        outboxService.saveProductOutboxEvent(savedProduct,OutboxEventType.CREATED,quantity);
    }

    @Transactional
    public void addCategoryAndSave(Long productId, Long categoryId) {
        // Nesne burada Transactional içinde yüklendiği için LAZY alanlara erişebilirsin!
        Product product = getProductOrThrow(productId);

        // İlişkili tabloya ekleme yap
        addCategoryMappingToProduct(product, categoryId, null);

        productRepository.save(product);
    }

    @Transactional
    public void addSubCategoryAndSave(Long productId, Long categoryId,Long subCategoryId) {
        Product product = getProductOrThrow(productId);

        updateOrAddCategoryMapping(product,categoryId,subCategoryId);

        productRepository.save(product);
    }

    // update
    @Transactional
    public void updateProductCore(Long productId,ProductRequest productRequest){
        Product updatedProduct = getProductOrThrow(productId);

        updateBasicFields(updatedProduct, productRequest);

        if (productRequest.categoryRequestList() != null) {
            List<ProductCategoryMapping> newMappings = mapToProductCategoryMappingEmbeddableList(
                    productRequest.categoryRequestList()
            );
            updatedProduct.getCategoryMappings().addAll(newMappings);
        }

        updatedProduct.setStatus(EntityStatus.UPDATING);
        updatedProduct.setStatusReason("Güncelleme isteği envantere gönderildi...");

        productRepository.save(updatedProduct);

        outboxService.saveProductOutboxEvent(updatedProduct, OutboxEventType.UPDATED,
                productRequest.quantity());
    }

    @Transactional
    public void deleteProduct(Long productId){
        Product deletedProduct = getProductOrThrow(productId);

        deletedProduct.setStatus(EntityStatus.DELETING);
        deletedProduct.setStatusReason("Silme işlemi onay bekliyor...");

        outboxService.saveProductOutboxEvent(deletedProduct,OutboxEventType.DELETED,0);
    }

    @Transactional
    public void removeCategoryFromProduct(Long productId, Long categoryId) {
        Product product = getProductOrThrow(productId);

        boolean removed = product.getCategoryMappings()
                .removeIf(m -> m.getCategoryId().equals(categoryId));

        if (!removed) {
            throw new CategoryNotFoundException(
                    "Category not found. ID: " + categoryId
            );
        }
    }

    @Transactional
    public void removeSubCategoryFromProduct(Long productId, Long subCategoryId) {
        Product product = getProductOrThrow(productId);

        boolean removed = product.getCategoryMappings()
                .removeIf(m -> m.getSubCategoryId().equals(subCategoryId));

        if (!removed) {
            throw new SubCategoryNotFoundException(
                    "Sub Category not found " + subCategoryId
            );
        }
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Ürün Bulunamadı !!! "+id));
    }

    private void addCategoryMappingToProduct(Product product, Long categoryId, Long subCategoryId) {
        List<ProductCategoryMapping> productCategoryMappingList = new ArrayList<>();
        productCategoryMappingList.add(new ProductCategoryMapping(categoryId, subCategoryId));

        product.getCategoryMappings().addAll(productCategoryMappingList);
    }

    private void updateOrAddCategoryMapping(Product product, Long categoryId, Long subCategoryId) {
        List<ProductCategoryMapping> mappingList = product.getCategoryMappings();

        // 1. Mevcut listede subCategory'si boş olan bir kayıt var mı bak ve güncelle
        boolean updated = false;
        for (ProductCategoryMapping mapping : mappingList) {
            if (mapping.getCategoryId().equals(categoryId) && mapping.getSubCategoryId() == null) {
                mapping.setSubCategoryId(subCategoryId);
                updated = true;
                break; // Bir tane bulup güncellememiz yeterli
            }
        }

        // 2. Eğer uygun boşluk bulunamadıysa yeni bir eşleşme ekle
        if (!updated) {
            addCategoryMappingToProduct(product, categoryId, subCategoryId);
        }
    }

    private void updateBasicFields(Product product, ProductRequest request) {
        if (request.productName() != null && !request.productName().isBlank()) {
            product.setProductName(request.productName());
        }

        if (request.productDescription() != null && !request.productDescription().isBlank()) {
            product.setProductDescription(request.productDescription());
        }

        if (request.productPrice() != null) {
            product.setProductPrice(request.productPrice());
        }
    }

    private List<ProductCategoryMapping> mapToProductCategoryMappingEmbeddableList(List<CategoryValidationRequest> categoryRequestList) {
        List<ProductCategoryMapping> productCategoryMappingList = new ArrayList<>();
        for (CategoryValidationRequest categoryValidationRequest : categoryRequestList) {
            for (Long subCategoryId : categoryValidationRequest.subCategoriesId()) {
                productCategoryMappingList.add(new ProductCategoryMapping(
                        categoryValidationRequest.categoryId(),
                        subCategoryId
                ));
            }
        }
        return productCategoryMappingList;
    }
}
