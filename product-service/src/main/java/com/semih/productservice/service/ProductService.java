package com.semih.productservice.service;

import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.common.dto.request.ProductCategoryInfoRequest;
import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.request.SubCategoryInfoRequest;
import com.semih.common.dto.response.ProductCategoryInfoResponse;
import com.semih.common.dto.response.ProductStockResponse;
import com.semih.common.exception.CategoryNotFoundException;
import com.semih.common.exception.SubCategoryNotFoundException;
import com.semih.productservice.client.CategoryClient;
import com.semih.productservice.client.InventoryClient;
import com.semih.productservice.client.SubCategoryClient;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.dto.response.ProductDetailResponse;
import com.semih.productservice.dto.response.ProductInfoResponse;
import com.semih.productservice.entity.Product;
import com.semih.productservice.entity.ProductCategoryMapping;
import com.semih.productservice.exception.ProductNotFoundException;
import com.semih.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryClient categoryClient;
    private final SubCategoryClient subCategoryClient;
    private final InventoryClient inventoryClient;

    public ProductService(ProductRepository productRepository, CategoryClient categoryClient, InventoryClient inventoryClient, SubCategoryClient subCategoryClient) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
        this.subCategoryClient = subCategoryClient;
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

        subCategoryClient.validateSubCategoryExists(categoryId,subCategoryId);

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

    @Transactional(readOnly=true)
    public List<ProductDetailResponse> getAllProductDetail(){
        List<Product> productList = productRepository.findAll();
        List<ProductDetailResponse> productDetailResponseList = new ArrayList<>();

        for(Product product:productList){
            List<ProductCategoryInfoResponse> categories = addCategoryResponses(product);

            ProductStockResponse stock = inventoryClient.getStockByProductId(product.getId()).getBody();

            ProductDetailResponse detailResponse = buildProductDetailResponse(product, categories, stock);
            productDetailResponseList.add(detailResponse);
        }

        return productDetailResponseList;
    }

    public String updateProductPartially(Long id, ProductRequest productRequest) {
        Product updatedProduct = getProductOrThrow(id);

        updateBasicFields(updatedProduct, productRequest);

        updateCategories(updatedProduct, productRequest);

        updateQuantity(updatedProduct, productRequest);

        productRepository.save(updatedProduct);

        return "Successfully";
    }

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
    private List<ProductCategoryMapping> mapToProductCategoryMappingEmbeddableList(List<CategoryValidationRequest>
                                                                                       categoryRequestList){
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

    //toResponse
    private ProductDetailResponse buildProductDetailResponse(Product product,
                                                             List<ProductCategoryInfoResponse> categories,
                                                             ProductStockResponse stockResponse) {

        ProductInfoResponse productInfoResponse = mapToProductInfoResponse(product);
        return new ProductDetailResponse(
                productInfoResponse,
                product.getProductDescription(),
                stockResponse,
                categories
        );
    }

    private ProductInfoResponse mapToProductInfoResponse(Product product){
        return new ProductInfoResponse(product.getId(),product.getProductName(),product.getProductPrice(),
                product.getCreatedAt(),product.getUpdatedAt());
    }

    private List<ProductCategoryInfoResponse> addCategoryResponses(
            Product product) {

        Map<Long,List<Long>> map = new HashMap<>();
        for(ProductCategoryMapping mapping : product.getCategoryMappings()) {
           map.computeIfAbsent(mapping.getCategoryId(),k-> new ArrayList<>()).add(mapping.getSubCategoryId());
        }

        List<ProductCategoryInfoRequest> productCategoryInfoRequests = new ArrayList<>();
        for(Map.Entry<Long,List<Long>> entry: map.entrySet()){
            List<SubCategoryInfoRequest> subCategoryInfoRequests = new ArrayList<>();
            for(Long subCategoryId:entry.getValue())
                subCategoryInfoRequests.add(new SubCategoryInfoRequest(subCategoryId));
            productCategoryInfoRequests.add(new ProductCategoryInfoRequest(entry.getKey(),subCategoryInfoRequests));
        }

        return categoryClient.getCategoryWithSubCategoriesForProductList(productCategoryInfoRequests).getBody();
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

    public void addCategoryMappingToProduct(Product product, Long categoryId, Long subCategoryId) {
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

        ProductQuantityRequest quantityRequest = mapToProductQuantityRequest(product.getId(), request.quantity());
        inventoryClient.createInventoryToProduct(quantityRequest);
    }

}
