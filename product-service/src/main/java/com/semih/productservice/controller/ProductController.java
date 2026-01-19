package com.semih.productservice.controller;

import com.semih.common.dto.request.ProductQuantityRequest;
import com.semih.common.dto.response.BasketProductResponse;
import com.semih.common.dto.response.ProductLineItemResponse;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.dto.response.ProductDetailResponse;
import com.semih.productservice.dto.response.ProductInfoResponse;
import com.semih.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(PRODUCTS)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PostMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<String> addCategoryToProduct(
            @PathVariable Long productId, @PathVariable Long categoryId) {

        return ResponseEntity.ok(productService.addCategoryToProduct(productId, categoryId));
    }

    @PostMapping("/{productId}/categories/{categoryId}/subcategories/{subCategoryId}")
    public ResponseEntity<String> addSubCategoryToProduct(
            @PathVariable Long productId, @PathVariable Long categoryId, @PathVariable Long subCategoryId) {

        return ResponseEntity.ok(productService.addSubCategoryToProduct(productId, categoryId, subCategoryId));
    }

    @PostMapping("/basket/products")
    public ResponseEntity<List<BasketProductResponse>> getBasketProducts(
            @RequestBody List<Long> productIdList) {
        return ResponseEntity.ok(
                productService.getBasketProductResponse(productIdList)
        );
    }

    @PostMapping(CHECKOUT_PRICE)
    public ResponseEntity<List<ProductLineItemResponse>> priceProductsForCheckout(
            @RequestBody List<ProductQuantityRequest> productQuantityRequests){
        List<ProductLineItemResponse> productLineItemResponseList = productService.
                priceProductsForCheckout(productQuantityRequests);
        return ResponseEntity.ok(productLineItemResponseList);
    }

    @GetMapping
    public ResponseEntity<List<ProductInfoResponse>> getAllProductInfo() {
        return ResponseEntity.ok(productService.getAllProductInfo());
    }

    @GetMapping("/details")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProductDetailResponse>> getAllProductDetail() {
        return ResponseEntity.ok(productService.getAllProductDetail());
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<String> updateProductPartially(
            @PathVariable Long productId, @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(productService.updateProductPartially(productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Boolean> deleteProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.deleteProductById(productId));
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ResponseEntity<Boolean> deleteProductByCategory(
            @PathVariable Long productId, @PathVariable Long categoryId) {

        return ResponseEntity.ok(productService.deleteProductByCategoryId(productId, categoryId));
    }

    @DeleteMapping("/{productId}/subcategories/{subCategoryId}")
    public ResponseEntity<Boolean> deleteProductBySubCategory(
            @PathVariable Long productId, @PathVariable Long subCategoryId) {

        return ResponseEntity.ok(productService.deleteProductBySubCategoryId(productId, subCategoryId));
    }
}
