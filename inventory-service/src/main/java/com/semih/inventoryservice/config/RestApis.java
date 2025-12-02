package com.semih.inventoryservice.config;

public class RestApis {

    private RestApis() {
    }

    public static final String DEVELOPER = "/dev";
    public static final String TEST = "/test";
    public static final String VERSIONS = "/v1";
    public static final String INVENTORY = DEVELOPER+VERSIONS+"/inventory";
    public static final String CREATE_INVENTORY_TO_PRODUCT ="/createInventoryToProduct";
    public static final String GET_INVENTORY_BY_PRODUCT_ID = "/getInventoryByProductId/{productId}";
    public static final String UPDATE_INVENTORY=  "/updateInventory";
    public static final String DELETE_INVENTORY = "/deleteInventory/{productId}";
}
