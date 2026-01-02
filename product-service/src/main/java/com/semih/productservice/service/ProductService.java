package com.semih.productservice.service;

import com.semih.common.dto.request.*;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryClient categoryClient;
    private final InventoryClient inventoryClient;

    public ProductService(ProductRepository productRepository, CategoryClient categoryClient,
                          InventoryClient inventoryClient) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
        this.inventoryClient = inventoryClient;
    }

    // Post
    public String createProduct(ProductRequest productRequest){
        if(productRequest.categoryRequestList()!=null && !productRequest.categoryRequestList().isEmpty())
            categoryClient.validateCategoryHierarchy(productRequest.categoryRequestList());

        Product savedProduct = productRepository.save(mapToCategoryEntity(productRequest));

        ProductQuantityRequest productQuantityRequest = mapToProductQuantityRequest(
                savedProduct.getId(),productRequest.quantity());
        inventoryClient.createInventoryToProduct(productQuantityRequest);

        return "Succesfully";
    }

    public String addCategoryToProduct(Long productId,Long categoryId){
        Product product = getProductOrThrow(productId);

        categoryClient.validateCategoryExistsById(categoryId);

        addCategoryMappingToProduct(product,categoryId,null);

        productRepository.save(product);

        return "Succesfully";
    }

    public String addSubCategoryToProduct(Long productId,Long categoryId,Long subCategoryId){
        Product product = getProductOrThrow(productId);

        categoryClient.validateSubCategoryExists(categoryId,subCategoryId);

        List<ProductCategoryMapping> productCategoryMappingList = productRepository.findByProductIdAndCategoryId(
                productId,categoryId
        );

        boolean hasSubCategory = false;
        for (ProductCategoryMapping productCategoryMapping : productCategoryMappingList) {
            if (productCategoryMapping.getSubCategoryId() == null) {
                hasSubCategory = true;
                productCategoryMapping.setSubCategoryId(subCategoryId);
                product.setCategoryMappings(productCategoryMappingList);
            }
        }


        if(!hasSubCategory)
            addCategoryMappingToProduct(product,categoryId,subCategoryId);

        productRepository.save(product);

        return "Succesfully";
    }

    // Get
    public List<ProductInfoResponse> getAllProductInfo(){
        return productRepository.findAll()
                .stream()
                .map(this::mapToProductInfoResponse)
                .collect(Collectors.toList());
    }

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


    public List<ProductDetailResponse> getAllProductDetail() {
        List<Product> productList = productRepository.findAllWithCategories();

        List<Long> productIdList = productList.stream().map(Product::getId).toList();
        List<ProductStockResponse> stockList = inventoryClient.getStockForProducts(productIdList)
                .getBody();

        // Map'i nesne olarak tutuyoruz
        Map<Long, ProductStockResponse> stockMap = (stockList != null) ? stockList.stream()
                .collect(Collectors.toMap(ProductStockResponse::productId, s -> s)) : new HashMap<>();

        // Kategori Request Hazırlama (Aynı Mantık)
        Map<Long, Set<Long>> globalCategoryRequestMap = new HashMap<>();
        for (Product product : productList) {
            for (ProductCategoryMapping mapping : product.getCategoryMappings()) {
                globalCategoryRequestMap.computeIfAbsent(mapping.getCategoryId(), k -> new HashSet<>())
                        .add(mapping.getSubCategoryId());
            }
        }

        List<ProductCategoryAndSubCategoryRequest> requests = globalCategoryRequestMap.entrySet().stream()
                .map(e -> new ProductCategoryAndSubCategoryRequest(e.getKey(), e.getValue()))
                .toList();

        List<ProductCategoryInfoResponse> categoryInfoList = categoryClient
                .getCategoryWithSubCategoriesForProductList(requests).getBody();

        Map<Long, ProductCategoryInfoResponse> categoryMap = (categoryInfoList != null) ?
                categoryInfoList.stream()
                .collect(Collectors.toMap(ProductCategoryInfoResponse::categoryId, c -> c))
                : new HashMap<>();

        // BİRLEŞTİRME
        return productList.stream().map(product -> {
            ProductInfoResponse info = new ProductInfoResponse(
                    product.getId(), product.getProductName(), product.getProductPrice(),
                    product.getCreatedAt(), product.getUpdatedAt());

            // Stok bilgisini nesne olarak çekiyoruz
            ProductStockResponse stock = stockMap.getOrDefault(product.getId(),
                    new ProductStockResponse(product.getId(), 0));

            // Gruplanmış Kategoriler
            Map<Long, List<Long>> productSpecificMapping = product.getCategoryMappings().stream()
                    .collect(Collectors.groupingBy(ProductCategoryMapping::getCategoryId,
                            Collectors.mapping(ProductCategoryMapping::getSubCategoryId, Collectors.toList())));

            List<ProductCategoryInfoResponse> finalCategories = productSpecificMapping.entrySet().stream()
                    .map(entry -> {
                        ProductCategoryInfoResponse fullCategory = categoryMap.get(entry.getKey());
                        if (fullCategory == null) return null;

                        List<SubCategoryInfoResponse> filteredSubs = fullCategory.subCategoryInfoResponses().stream()
                                .filter(sub -> entry.getValue().contains(sub.subCategoryId()))
                                .toList();

                        return new ProductCategoryInfoResponse(fullCategory.categoryId(), fullCategory.categoryName(), filteredSubs);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return new ProductDetailResponse(info, product.getProductDescription(), stock, finalCategories);
        }).toList();
    }

//    public void validateProductsAvailability(List<ProductQuantityRequest> requests){
//        // hem ürün hemde stok yeterlı mıktarda mı kontrolu edılıyo.
//        inventoryClient.checkProductsAvailability(requests);
//    }

    public List<BasketProductResponse> getBasketProductResponse(List<Long> productIdList){
        List<Product> products = findByProductIdIn(productIdList);

        List<ProductStockResponse> productStockResponses = inventoryClient.
                    getStockForProducts(productIdList)
                    .getBody();

        return mapToBasketProductResponse(products,productStockResponses);
    }

    // Update
    public String updateProductPartially(Long id, ProductRequest productRequest) {
        Product updatedProduct = getProductOrThrow(id);

        updateBasicFields(updatedProduct, productRequest);

        updateCategories(updatedProduct, productRequest);

        updateQuantity(updatedProduct, productRequest);

        productRepository.save(updatedProduct);

        return "Successfully";
    }

    // Delete
    public Boolean deleteProductById(Long productId){
        Product deletedCategory = getProductOrThrow(productId);

        inventoryClient.deleteInventoryByProductId(deletedCategory.getId());
        productRepository.delete(deletedCategory);

        return true;
    }

    public Boolean deleteProductByCategoryId(Long productId,Long categoryId){
        Product product = getProductOrThrow(productId);


        List<ProductCategoryMapping> productCategoryMappingList = product.getCategoryMappings();

        boolean isFound = false;
        for(ProductCategoryMapping productCategoryMapping:productCategoryMappingList){
            if(productCategoryMapping.getCategoryId().equals(categoryId)){
                isFound = true;
                break;
            }
        }

        if(!isFound)
            throw new CategoryNotFoundException("Kategori bulunamadı. ID: " + categoryId);

        productCategoryMappingList.removeIf(productCategoryMapping -> productCategoryMapping.getCategoryId()
                .equals(categoryId));

        productRepository.save(product);

        return true;
    }

    public Boolean deleteProductBySubCategoryId(Long productId,Long subCategoryId){
        Product product = getProductOrThrow(productId);

        List<ProductCategoryMapping> productCategoryMappingList = product.getCategoryMappings();

        boolean isFound = false;
        for(ProductCategoryMapping productCategoryMapping:productCategoryMappingList){
            if(productCategoryMapping.getSubCategoryId().equals(subCategoryId)){
                isFound = true;
                break;
            }
        }

        if(!isFound)
            throw new SubCategoryNotFoundException("Alt Kategori Bulunamadı "+subCategoryId);

        productCategoryMappingList.removeIf(productCategoryMapping -> productCategoryMapping.getSubCategoryId()
                .equals(subCategoryId));

        productRepository.save(product);

        return true;
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
                    productRequest.productName(),
                    productRequest.productDescription(),
                    productRequest.productPrice(),
                    mapToProductCategoryMappingEmbeddableList(productRequest.categoryRequestList())
            );
        }

        return new Product(
                productRequest.productName(),
                productRequest.productDescription(),
                productRequest.productPrice()
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

    private void addCategoryMappingToProduct(Product product, Long categoryId, Long subCategoryId) {
        List<ProductCategoryMapping> productCategoryMappingList = new ArrayList<>();
        productCategoryMappingList.add(new ProductCategoryMapping(categoryId, subCategoryId));

        product.getCategoryMappings().addAll(productCategoryMappingList);
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

    private void updateCategories(Product product, ProductRequest request) {
        if (request.categoryRequestList() == null || request.categoryRequestList().isEmpty()) return;

        categoryClient.validateCategoryHierarchy(request.categoryRequestList());

        List<ProductCategoryMapping> mappings = product.getCategoryMappings();
        mappings.addAll(mapToProductCategoryMappingEmbeddableList(request.categoryRequestList()));

        product.setCategoryMappings(mappings);
    }

    private void updateQuantity(Product product, ProductRequest request) {
        if (request.quantity() == null) return;

        ProductQuantityRequest quantityRequest = mapToProductQuantityRequest(product.getId(),
                request.quantity());

        inventoryClient.createInventoryToProduct(quantityRequest);
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
