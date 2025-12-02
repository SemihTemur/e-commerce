package com.semih.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static com.semih.productservice.config.RestApis.*;

@FeignClient(name = "subCategory",url = "http://localhost:8085/dev/v1/subCategory")
public interface SubCategoryClient {

    @GetMapping(VALIDATE_SUB_CATEGORY_EXISTS_BY_ID)
     ResponseEntity<Void> validateSubCategoryExists(
            @PathVariable Long categoryId, @PathVariable Long subCategoryId);
}
