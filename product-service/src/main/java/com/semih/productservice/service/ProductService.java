package com.semih.productservice.service;

import com.semih.common.constant.EntityStatus;
import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryAndSubCategoryRequest;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.*;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import com.semih.productservice.client.CategoryClient;
import com.semih.productservice.client.InventoryClient;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.dto.response.ProductDetailResponse;
import com.semih.productservice.dto.response.ProductInfoResponse;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductCategoryMapping;
import com.semih.productservice.exception.ProductNotFoundException;
import com.semih.productservice.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductManager productManager;

    private final ProductRepository productRepository;

    private final CategoryClient categoryClient;

    private final InventoryClient inventoryClient;

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductManager productManager,
                          ProductRepository productRepository,
                          CategoryClient categoryClient, InventoryClient inventoryClient) {
        this.productManager = productManager;
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
        this.inventoryClient = inventoryClient;
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "createProductCategoryServiceFallback")
    public String createProduct(ProductRequest productRequest){
        if(productRequest.categoryRequestList()!=null && !productRequest.categoryRequestList().isEmpty())
            categoryClient.validateCategoryHierarchy(productRequest.categoryRequestList());

        Product savedProduct = mapToCategoryEntity(productRequest);
        productManager.persistProductAndOutbox(savedProduct,productRequest.quantity());

        return "Succesfully";
    }

    public String createProductCategoryServiceFallback(ProductRequest productRequest, Throwable t) {
        log.error("Category Service unavailable while creating product. Reason: {}", t.getMessage());
        return "Category service is temporarily unavailable. Please try again later.";
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "addCategoryToProductCategoryServiceFallback")
    public String addCategoryToProduct(Long productId, Long categoryId) {
        // 1. Dış servis çağrısını burada yap (Transaction yok, temiz)
        categoryClient.validateCategoryExistsById(categoryId);

        // 2. Sadece ID'leri gönder, nesneyi Transactional serviste yükle
        productManager.addCategoryAndSave(productId, categoryId);

        return "Successfully";
    }

    public String addCategoryToProductCategoryServiceFallback(
            Long productId,
            Long categoryId,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while adding category to product. productId={}, categoryId={}",
                productId,
                categoryId,
                t
        );

        return "Category service is temporarily unavailable. Category could not be added to product.";
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "addSubCategoryToProductCategoryServiceFallback")
    public String addSubCategoryToProduct(Long productId,Long categoryId,Long subCategoryId){
        categoryClient.validateSubCategoryExists(categoryId,subCategoryId);

        productManager.addSubCategoryAndSave(productId,categoryId,subCategoryId);

        return "Succesfully";
    }

    public String addSubCategoryToProductCategoryServiceFallback(
            Long productId,
            Long categoryId,
            Long subCategoryId,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while adding subcategory to product. productId={}, categoryId={}, subCategoryId={}",
                productId,
                categoryId,
                subCategoryId,
                t
        );

        return "Category service is temporarily unavailable. Subcategory could not be added to product.";
    }


    // Get
    public List<ProductInfoResponse> getAllProductInfo(){
        return productRepository.findAll()
                .stream()
                .map(this::mapToProductInfoResponse)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "priceProductsForCheckoutInventoryServiceFallback")
    public List<ProductLineItemResponse> priceProductsForCheckout(
            List<ProductQuantityRequest> productQuantityRequests) {

        inventoryClient.checkAvailabilityByProductIds(productQuantityRequests);

        List<Long> productIds = productQuantityRequests
                .stream()
                .map(ProductQuantityRequest::productId)
                .toList();

        List<Product> productList = productRepository.findByIdIn(productIds);

        validateAllProductsExist(productIds, productList);

        Map<Long,Integer> productQuantityRequestMap = productQuantityRequests
                .stream()
                .collect(Collectors.toMap(
                        ProductQuantityRequest::productId,
                        ProductQuantityRequest::quantity
                ));

        return mapToProductLineItemResponse(productList, productQuantityRequestMap);
    }

    public List<ProductLineItemResponse> priceProductsForCheckoutInventoryServiceFallback(
            List<ProductQuantityRequest> productQuantityRequests,
            Throwable t
    ) {
        log.error(
                "Inventory Service unavailable during checkout pricing. Requests={}",
                productQuantityRequests,
                t
        );

        return Collections.emptyList();
    }

    public List<ProductDetailResponse> getAllProductDetail() {

        List<Product> productList = productRepository.findAllWithCategories();

        List<Long> productIdList = productList.stream()
                .map(Product::getId)
                .toList();

        // 🔹 AYRI METOT
        Map<Long, ProductStockResponse> stockMap = fetchStockMap(productIdList);

        // 🔹 AYRI METOT
        Map<Long, ProductCategoryInfoResponse> categoryMap = fetchCategoryMap(productList);

        // 🔹 BİRLEŞTİRME (AYNI)
        return productList.stream().map(product -> {

            ProductInfoResponse info = new ProductInfoResponse(
                    product.getId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    product.getCreatedAt(),
                    product.getUpdatedAt()
            );

            ProductStockResponse stock = stockMap.getOrDefault(
                    product.getId(),
                    new ProductStockResponse(product.getId(), 0)
            );

            Map<Long, List<Long>> productSpecificMapping =
                    product.getCategoryMappings().stream()
                            .collect(Collectors.groupingBy(
                                    ProductCategoryMapping::getCategoryId,
                                    Collectors.mapping(
                                            ProductCategoryMapping::getSubCategoryId,
                                            Collectors.toList()
                                    )
                            ));

            List<ProductCategoryInfoResponse> finalCategories =
                    productSpecificMapping.entrySet().stream()
                            .map(entry -> {
                                ProductCategoryInfoResponse fullCategory =
                                        categoryMap.get(entry.getKey());

                                if (fullCategory == null) return null;

                                List<SubCategoryInfoResponse> filteredSubs =
                                        fullCategory.subCategoryInfoResponses().stream()
                                                .filter(sub ->
                                                        entry.getValue()
                                                                .contains(sub.subCategoryId()))
                                                .toList();

                                return new ProductCategoryInfoResponse(
                                        fullCategory.categoryId(),
                                        fullCategory.categoryName(),
                                        filteredSubs
                                );
                            })
                            .filter(Objects::nonNull)
                            .toList();

            return new ProductDetailResponse(
                    info,
                    product.getProductDescription(),
                    stock,
                    finalCategories
            );

        }).toList();
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fetchStockMapInventoryServiceFallback")
    public Map<Long, ProductStockResponse> fetchStockMap(List<Long> productIdList) {

        List<ProductStockResponse> stockList = inventoryClient
                .getStockForProducts(productIdList)
                .getBody();

        return (stockList != null)
                ? stockList.stream()
                .collect(Collectors.toMap(ProductStockResponse::productId, s -> s))
                : new HashMap<>();
    }

    public Map<Long, ProductStockResponse> fetchStockMapInventoryServiceFallback(
            List<Long> productIdList,
            Throwable t
    ) {
        log.error(
                "Inventory Service unavailable while fetching stock info. productIds={}",
                productIdList,
                t
        );

        return new HashMap<>();
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "fetchCategoryMapCategoryServiceFallback")
    public Map<Long, ProductCategoryInfoResponse> fetchCategoryMap(
            List<Product> productList
    ) {

        Map<Long, Set<Long>> globalCategoryRequestMap = new HashMap<>();

        for (Product product : productList) {
            for (ProductCategoryMapping mapping : product.getCategoryMappings()) {
                globalCategoryRequestMap
                        .computeIfAbsent(mapping.getCategoryId(), k -> new HashSet<>())
                        .add(mapping.getSubCategoryId());
            }
        }

        List<ProductCategoryAndSubCategoryRequest> requests =
                globalCategoryRequestMap.entrySet().stream()
                        .map(e -> new ProductCategoryAndSubCategoryRequest(
                                e.getKey(),
                                e.getValue()
                        ))
                        .toList();

        List<ProductCategoryInfoResponse> categoryInfoList = categoryClient
                .getCategoryWithSubCategoriesForProductList(requests)
                .getBody();

        return (categoryInfoList != null)
                ? categoryInfoList.stream()
                .collect(Collectors.toMap(
                        ProductCategoryInfoResponse::categoryId,
                        c -> c
                ))
                : new HashMap<>();
    }

    public Map<Long, ProductCategoryInfoResponse> fetchCategoryMapCategoryServiceFallback(
            List<Product> productList,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while fetching category map. productCount={}",
                productList != null ? productList.size() : 0,
                t
        );

        return new HashMap<>();
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getBasketProductResponseInventoryServiceFallback")
    public List<BasketProductResponse> getBasketProductResponse(List<Long> productIdList){
        List<Product> products = findByProductIdIn(productIdList);

        List<ProductStockResponse> productStockResponses = inventoryClient.
                    getStockForProducts(productIdList)
                    .getBody();

        return mapToBasketProductResponse(products,productStockResponses);
    }

    public List<BasketProductResponse> getBasketProductResponseInventoryServiceFallback(
            List<Long> productIdList,
            Throwable t
    ) {
        log.error(
                "Inventory Service unavailable while fetching basket product response. productIds={}",
                productIdList,
                t
        );

        return Collections.emptyList();
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "validateCategoryStructureCategoryServiceFallback")
    public void validateCategoryStructure(ProductRequest request) {
        if (request.categoryRequestList() != null && !request.categoryRequestList().isEmpty()) {
            categoryClient.validateCategoryHierarchy(request.categoryRequestList());
        }
    }

    public void validateCategoryStructureCategoryServiceFallback(
            ProductRequest request,
            Throwable t
    ) {
        log.error(
                "Category Service unavailable while validating category structure. request={}",
                request,
                t
        );
        throw new RuntimeException("Category service is temporarily unavailable");
    }

    // Update
    public String updateProductPartially(Long id, ProductRequest productRequest) {
        validateCategoryStructure(productRequest);

        productManager.updateProductCore(id,productRequest);

        return "Successfully";
    }

    public Boolean deleteProductById(Long productId) {
        productManager.deleteProduct(productId);
        return true;
    }

    public Boolean deleteProductByCategoryId(Long productId, Long categoryId) {
        productManager.removeCategoryFromProduct(productId, categoryId);
        return true;
    }

    public Boolean deleteProductBySubCategoryId(Long productId, Long subCategoryId) {
        productManager.removeSubCategoryFromProduct(productId, subCategoryId);
        return true;
    }

    //For kafka
    @Transactional
    public void completeProductStatus(ProductStockResponseEvent productStockResponseEvent){
        productRepository
                .findByIdAndPendingStatus(productStockResponseEvent.productId())
                .ifPresentOrElse(
                        product -> {
                            applyStatusFromStockResponse(product,productStockResponseEvent);

                            log.info(
                                    "Product {} status updated to {}",
                                    product.getId(),
                                    product.getStatus()
                            );
                        },
                        () -> log.warn(
                                "No PENDING product found to update: {}",
                                productStockResponseEvent.productId()
                        )
                );

    }

    private void applyStatusFromStockResponse(
            Product product,
            ProductStockResponseEvent productStockResponseEvent
    ) {
        switch (productStockResponseEvent.operation()) {

            case ACTIVE -> product.setStatus(EntityStatus.ACTIVE);

            case UPDATING -> product.setStatus(EntityStatus.UPDATING);

            case REJECTED -> product.setStatus(EntityStatus.REJECTED);

            default -> {
                log.warn(
                        "Unhandled operation status: {} for productId: {}",
                        productStockResponseEvent.operation(),
                        product.getId()
                );
                return;
            }
        }

        product.setStatusReason(productStockResponseEvent.message());
    }

    private String getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || authentication.getPrincipal()==null){
            throw new RuntimeException("Böyle bir Kullanıcı yoktur");
        }

        return (String) authentication.getPrincipal();
    }

    //Mapping Client
    private ProductQuantityRequest mapToProductQuantityRequest(Long productId, Integer quantity){
        return new ProductQuantityRequest(productId,quantity);
    }

    //toEntity
    private List<ProductCategoryMapping> mapToProductCategoryMappingEmbeddableList(
            List<CategoryValidationRequest> categoryRequestList){
        List<ProductCategoryMapping> productCategoryMappingList = new ArrayList<>();

        for(CategoryValidationRequest categoryValidationRequest:categoryRequestList){
            for(Long subCategoryId:categoryValidationRequest.subCategoriesId())
                productCategoryMappingList.add(new ProductCategoryMapping(
                        categoryValidationRequest.categoryId(),
                        subCategoryId
                ));
        }
        return productCategoryMappingList;
    }

    private Product mapToCategoryEntity(ProductRequest productRequest){
        if(productRequest.categoryRequestList()!=null && !productRequest.categoryRequestList().isEmpty()) {
            return new Product(
                    getUserId(),
                    productRequest.productName(),
                    productRequest.productDescription(),
                    productRequest.productPrice(),
                    "Waiting for review",
                    mapToProductCategoryMappingEmbeddableList(productRequest.categoryRequestList())
            );
        }

        return new Product(
                getUserId(),
                productRequest.productName(),
                productRequest.productDescription(),
                productRequest.productPrice(),
                "Waiting for review"
        );

    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Ürün Bulunamadı !!! "+id));
    }

    private List<Product> findByProductIdIn(List<Long> productIds){
        List<Product> products = productRepository.findByIdIn(productIds);

        if(products.size()!=productIds.size())
            throw new RuntimeException("Eksik ürün var"); // BasketItemNotFoundException

        return products;
    }

    //toResponse
    private ProductInfoResponse mapToProductInfoResponse(Product product){
        return new ProductInfoResponse(product.getId(),product.getProductName(),product.getProductPrice(),
                product.getCreatedAt(),product.getUpdatedAt());
    }

    private List<BasketProductResponse> mapToBasketProductResponse(
            List<Product> products, List<ProductStockResponse> productStockResponses){
       List<BasketProductResponse> basketProductResponses = new ArrayList<>();

        for(int i = 0;i<products.size();i++){
           basketProductResponses.add(
                   new BasketProductResponse(
                           products.get(i).getId(),
                           products.get(i).getProductName(),
                           products.get(i).getProductPrice(),
                           productStockResponses.get(i)
                   )
           );
        }

        return basketProductResponses;
    }

    private List<ProductLineItemResponse> mapToProductLineItemResponse(List<Product> productList,
                                                                       Map<Long, Integer>
                                                                               productQuantityRequestMap){
        List<ProductLineItemResponse> productLineItemResponseList = new ArrayList<>();

        for(Product product:productList){
            productLineItemResponseList.add(new ProductLineItemResponse(
                    product.getId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    productQuantityRequestMap.get(product.getId())
            ));
        }

        return productLineItemResponseList;
    }

    private void validateAllProductsExist(List<Long> requestedProductIds, List<Product> foundProducts) {
        Set<Long> requestedIds = new HashSet<>(requestedProductIds);

        Set<Long> foundIds = foundProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (!foundIds.containsAll(requestedIds)) {
            requestedIds.removeAll(foundIds);

            throw new ProductNotFoundException(
                    "Product(s) not found: " + requestedIds
            );
        }
    }

}
