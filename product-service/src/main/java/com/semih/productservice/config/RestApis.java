package com.semih.productservice.config;

public class RestApis {
    // Product
    public static final String DEVELOPER = "/dev";
    public static final String TEST = "/test";
    public static final String VERSIONS = "/v1";
    public static final String PRODUCT = DEVELOPER+VERSIONS+"/product";
    public static final String CREATE_PRODUCT = "/createProduct";
    public static final String ADD_CATEGORY_TO_PRODUCT_BY_ID = "/product/{productId}/categories/{categoryId}";
    public static final String ADD_SUBCATEGORY_TO_PRODUCT = "/products/{productId}/categories/{categoryId}/subcategories";
    public static final String GET_PRODUCT_INFO = "/getProductInfo";
    public static final String GET_PRODUCT_DETAILS = "/getProductDetails";
    public static final String UPDATE_PRODUCT = "/updateProduct/{id}";
    public static final String DELETE_PRODUCT_BY_ID = "/deleteProductById/{productId}";
    public static final String DELETE_PRODUCT_BY_CATEGORY_ID = "/deleteProductByCategoryId/{productId}/{categoryId}";
    public static final String DELETE_PRODUCT_BY_SUB_CATEGORY_ID = "/deleteProductBySubCategoryId/{productId}/{subCategoryId}";

    //Category
    public static final String VALIDATE_CATEGORY_HIERARCHY = "/validateCategoryHierarchy";
    public static final String GET_CATEGORY_WITH_SUBCATEGORIES_BY_ID = "/getCategoryWıthSubCategoriesById/{categoryId}";
    public static final String GET_CATEGORY_WITH_SUBCATEGORIES_FOR_PRODUCT = "/getCategoryWithSubcategoriesForProduct";
    public static final String EXISTS_CATEGORY_WITH_SUBCATEGORIES = "/existsCategoryWithSubCategories";

    // Inventory
    public static final String CREATE_INVENTORY_TO_PRODUCT ="/createInventoryToProduct";
    public static final String GET_INVENTORY_BY_PRODUCT_ID = "/getInventoryByProductId";
    public static final String UPDATE_INVENTORY=  "/updateInventory";
    public static final String DELETE_INVENTORY = "/deleteInventory";

}
