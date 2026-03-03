package com.semih.productservice.service;

import com.semih.common.constant.EntityStatus;
import com.semih.common.constant.OutboxEventType;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.response.ProductStockResponseEvent;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import com.semih.productservice.constant.ProductStatus;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.entity.ProcessedEvent;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductCategoryMapping;
import com.semih.productservice.entity.ProductUpdate;
import com.semih.productservice.exception.ProductNotFoundException;
import com.semih.productservice.repository.ProcessedEventRepository;
import com.semih.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class ProductManager {

    private final ProductRepository productRepository;

    private final OutboxService outboxService;

    private final ProductUpdateService productUpdateService;

    private final ProcessedEventRepository processedEventRepository;

    private final ProductDeleteService productDeleteService;

    private static final Logger logger = LoggerFactory.getLogger(ProductManager.class);

    public ProductManager(ProductRepository productRepository, OutboxService outboxService,
                          ProductUpdateService productUpdateService,
                          ProcessedEventRepository processedEventRepository,
                          ProductDeleteService productDeleteService) {
        this.productRepository = productRepository;
        this.outboxService = outboxService;
        this.productUpdateService = productUpdateService;
        this.processedEventRepository = processedEventRepository;
        this.productDeleteService = productDeleteService;
    }

    // create
    // kafkadan onay gelene kadar pendıngde
    @Transactional
    public void persistProductAndOutbox(Product savedProduct,Integer quantity) {
        // Ürünü kaydet
        productRepository.save(savedProduct);

        outboxService.saveProductOutboxEvent(savedProduct,OutboxEventType.CREATED,quantity);
    }

    // kafkadan onay gelırse actıve çek
    @Transactional
    public void applyCreate(Product product,String reasonMessage){
        product.setStatus(ProductStatus.ACTIVE);
        product.setStatusReason(reasonMessage);

        productRepository.save(product);
    }

    // kafkadan red gelırse rejected çek
    @Transactional
    public void applyRejected(Product product,String reasonMessage){
        product.setStatus(ProductStatus.REJECTED);
        product.setStatusReason(reasonMessage);

        productRepository.save(product);
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
    // önce ınventory-servısten cevap gelene kadar processıngde bekle
    @Transactional
    public void updateProductCore(Long productId, ProductRequest productRequest) {
        Product product = getProductOrThrow(productId);

        // 1️⃣ Product durum
        product.setStatus(ProductStatus.PROCESSING);
        product.setStatusReason("Update request sent to inventory service");

        // 2️⃣ Update taslağı → DELEGE
        productUpdateService.createUpdateDraft(product, productRequest);

        // 3️⃣ Outbox
        outboxService.saveProductOutboxEvent(
                product,
                OutboxEventType.UPDATED,
                productRequest.quantity()
        );
    }

    // cevap actıve ıse ıslemı yap
    @Transactional
    public void applyUpdate(Product product,String reasonMessage) {
        ProductUpdate productUpdate = productUpdateService.getProductUpdateFindById(product.getId());

        product.setProductName(productUpdate.getName());
        product.setProductDescription(productUpdate.getProductDescription());
        product.setProductPrice(productUpdate.getPrice());

        product.setStatus(ProductStatus.ACTIVE);
        product.setStatusReason(reasonMessage);

        productRepository.save(product);

        productUpdateService.applyApprovedUpdate(productUpdate);
    }

    // cevap rejected ise işlem yap
    @Transactional
    public void rejectUpdate(Product product,String reasonMessage) {
        product.setStatus(ProductStatus.REJECTED);
        product.setStatusReason(reasonMessage);

        productRepository.save(product);

        productUpdateService.rejectUpdateApply(product.getId());
    }

    @Transactional
    public void revertToActiveStatus(Long id){
        Product updateProduct = productRepository.findRejectedProductById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product is not found" + id));

        updateProduct.setStatus(ProductStatus.ACTIVE);
        updateProduct.setStatusReason("Reverted to ACTIVE after rejection.");
    }

    // silme isteği gonder
    @Transactional
    public void deleteProduct(Long productId){
        Product product = getProductOrThrow(productId);

        product.setStatus(ProductStatus.PROCESSING);
        product.setStatusReason("Deletion request is pending approval.");

        productDeleteService.createDeleteRequest(product);

        outboxService.saveProductOutboxEvent(
                product,
                OutboxEventType.DELETED,
                0
        );
    }

    // sılme ıslemı actıve ıse
    @Transactional
    public void deleteProductPermanently(Product product,String reasonMessage) {
        product.setStatus(ProductStatus.DELETED);
        product.setStatusReason(reasonMessage);

        productRepository.save(product);

        productDeleteService.confirmDeletionApply(product.getId());
    }

    @Transactional
    public void rejectedDeleteProduct(Product product,String reasonMessage){
        product.setStatus(ProductStatus.REJECTED);
        product.setStatusReason(reasonMessage);

        productRepository.save(product);

        productDeleteService.rejectDeletionApply(product.getId());
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

    @Transactional
    public void completeProductStatus(ProductStockResponseEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            logger.info("Event zaten işlenmiş, atlanıyor: {}", event.eventId());
            return;
        }

        try {
            Product product = getProductOrThrow(event.productId());
            String reasonMessage = event.reason();

            processStatusChange(event, product, reasonMessage);

            processedEventRepository.saveAndFlush(new ProcessedEvent(event.eventId()));

        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            logger.warn("Mükerrer event veya yarış durumu yakalandı, sorun yok: {}", event.eventId());

        } catch (Exception e) {
            logger.error("Event işlenirken hata oluştu: {}", event.eventId(), e);
            throw e;
        }
    }

    public void processStatusChange(ProductStockResponseEvent event, Product product, String reasonMessage) {
        switch (event.outboxEventType()) {
            case CREATED -> {
                if (event.operation() == EntityStatus.ACTIVE) applyCreate(product, reasonMessage);
                else applyRejected(product, reasonMessage);
            }
            case UPDATED -> {
                if (event.operation() == EntityStatus.ACTIVE) applyUpdate(product, reasonMessage);
                else rejectUpdate(product, reasonMessage);
            }
            case DELETED -> {
                if (event.operation() == EntityStatus.ACTIVE) deleteProductPermanently(product, reasonMessage);
                else rejectedDeleteProduct(product, reasonMessage);
            }
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
