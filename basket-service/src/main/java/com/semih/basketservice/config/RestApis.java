package com.semih.basketservice.config;

public class RestApis {

    public static final String DEVELOPER = "/dev";
    public static final String TEST = "/test";
    public static final String VERSIONS = "/v1";

    // Basket
    public static final String BASKET_SERVICE = DEVELOPER + VERSIONS + "/basket";

    public static final String ADD_BASKET_ITEM = "/items";          // POST
    public static final String GET_BASKET_ITEM_LIST = "/items";    // GET
    public static final String DELETE_BASKET_ITEM = "/items/{id}"; // DELETE


    // Product
    public static final String PRODUCT = DEVELOPER+VERSIONS+"/product";
    public static final String CHECK_AVAILABILITY_BY_PRODUCT_ID = "/checkAvailabilityByProductId";
    public static final String GET_BASKET_PRODUCT_BY_ID = "/basket/product/{productId}";

}
