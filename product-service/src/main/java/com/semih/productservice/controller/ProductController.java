package com.semih.productservice.controller;

import com.semih.common.dto.request.CategoryValidationRequest;
import com.semih.productservice.dto.request.ProductRequest;
import com.semih.productservice.dto.response.ProductDetailResponse;
import com.semih.productservice.dto.response.ProductInfoResponse;
import com.semih.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.semih.productservice.config.RestApis.*;

@RestController
@RequestMapping(PRODUCT)
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(CREATE_PRODUCT)
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductRequest productRequest){
        String message = productService.createProduct(productRequest);
        return ResponseEntity.ok(message);
    }

    @PostMapping(ADD_CATEGORY_TO_PRODUCT_BY_ID)
    public ResponseEntity<String> addCategoryToProduct(@PathVariable Long productId,@PathVariable Long categoryId){
        String message = productService.addCategoryToProduct(productId,categoryId);
        return ResponseEntity.ok(message);
    }

    @GetMapping(GET_PRODUCT_INFO)
    public ResponseEntity<List<ProductInfoResponse>> getAllProductInfo(){
        List<ProductInfoResponse> productInfoResponseList = productService.getAllProductInfo();
        return ResponseEntity.ok(productInfoResponseList);
    }

    @GetMapping(GET_PRODUCT_DETAILS)
    public ResponseEntity<List<ProductDetailResponse>> getAllProductDetail(){
        List<ProductDetailResponse> productDetailResponseList = productService.getAllProductDetail();
        return ResponseEntity.ok(productDetailResponseList);
    }

    @PatchMapping(UPDATE_PRODUCT)
    public ResponseEntity<String> updateProductPartially(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest productRequest){
        String message = productService.updateProductPartially(id, productRequest);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping(DELETE_PRODUCT_BY_ID)
    public ResponseEntity<Boolean> deleteProductById(@PathVariable Long productId){
        return ResponseEntity.ok(productService.deleteProductById(productId));
    }

    @DeleteMapping(DELETE_PRODUCT_BY_CATEGORY_ID)
    public ResponseEntity<Boolean> deleteProductByCategoryId(@PathVariable Long productId,
                                                             @PathVariable Long categoryId){
        return ResponseEntity.ok(productService.deleteProductByCategoryId(productId, categoryId));
    }

    @DeleteMapping(DELETE_PRODUCT_BY_SUB_CATEGORY_ID)
    public ResponseEntity<Boolean> deleteProductBySubCategoryId(@PathVariable Long productId,
                                                                @PathVariable Long subCategoryId){
        return ResponseEntity.ok(productService.deleteProductBySubCategoryId(productId, subCategoryId));
    }

}
