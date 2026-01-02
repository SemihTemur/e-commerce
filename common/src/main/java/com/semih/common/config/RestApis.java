package com.semih.common.config;

public final class RestApis {

    private RestApis() {
    }

    // Base environment & version
    public static final String DEV = "/dev";
    public static final String TEST = "/test";
    public static final String V1 = "/v1";

    // Base resources
    public static final String PRODUCTS = DEV + V1 + "/products";
    public static final String BASKETS = DEV + V1 + "/baskets";
    public static final String INVENTORIES = DEV + V1 + "/inventories";
    public static final String CATEGORIES = DEV + V1 + "/categories";
    public static final String SUBCATEGORIES = DEV + V1 + "/subcategories";
    public static final String USERS = DEV + V1 + "/users";
    public static final String ORDERS = DEV + V1 + "/orders";

    // Common product actions
    public static final String CHECK_AVAILABILITY = "/check-availability";
    public static final String BASKET_PRODUCT = "/basket/products";
    public static final String CHECKOUT_PRICE = "/checkout/price";

    // Common category actions
    public static final String VALIDATE_CATEGORY_HIERARCHY = "/validate-hierarchy";
    public static final String EXISTS_WITH_SUBCATEGORIES = "/exists-with-subcategories";
    public static final String VALIDATE_CATEGORY_EXISTS_BY_ID = "/validate/{categoryId}";
    public static final String FOR_PRODUCT = "/for-product";

    // Inventory common actions
    public static final String CREATE_TO_PRODUCT = "/create-to-product";
    public static final String STOCKS = "/stocks";
    public static final String UPDATE = "/update";
    public static final String DELETE_BY_PRODUCT_ID = "/{productId}";
    public static final String VALIDATE_FOR_CHECKOUT = "/validate-for-checkout";
}
