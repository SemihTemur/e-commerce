package com.semih.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "subCategory")
public interface SubCategoryClient {

}
