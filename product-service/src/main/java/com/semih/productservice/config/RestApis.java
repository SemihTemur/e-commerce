package com.semih.productservice.config;

public class RestApis {

    public static final String DEVELOPER = "/dev";
    public static final String TEST = "/test";
    public static final String VERSIONS = "/v1";

    // Product
    public static final String PRODUCT = DEVELOPER+VERSIONS+"/product";
    public static final String CREATE_PRODUCT = "/createProduct";
    public static final String ADD_CATEGORY_TO_PRODUCT_BY_ID = "/product/{productId}/categories/{categoryId}";
    public static final String ADD_SUBCATEGORY_TO_PRODUCT =
            "/products/{productId}/categories/{categoryId}/subcategories/{subCategoryId}";
    public static final String GET_PRODUCT_INFO = "/getProductInfo";
    public static final String GET_PRODUCT_DETAILS = "/getProductDetails";
    public static final String CHECK_AVAILABILITY_BY_PRODUCT_ID = "/checkAvailabilityByProductId";
    public static final String GET_BASKET_PRODUCT_BY_ID = "/basket/product/{productId}";
    public static final String UPDATE_PRODUCT = "/updateProduct/{id}";
    public static final String DELETE_PRODUCT_BY_ID = "/deleteProductById/{productId}";
    public static final String DELETE_PRODUCT_BY_CATEGORY_ID = "/deleteProductByCategoryId/{productId}/{categoryId}";
    public static final String DELETE_PRODUCT_BY_SUB_CATEGORY_ID =
            "/deleteProductBySubCategoryId/{productId}/{subCategoryId}";

    //Category
    public static final String CATEGORY = DEVELOPER+VERSIONS+"/category";
    public static final String VALIDATE_CATEGORY_HIERARCHY = "/validateCategoryHierarchy";
    public static final String VALIDATE_CATEGORY_EXISTS_BY_ID = "/category/validate/{categoryId}";
    public static final String GET_CATEGORY_WITH_SUBCATEGORIES_FOR_PRODUCT =
            "/getCategoryWithSubcategoriesForProduct";
    public static final String EXISTS_CATEGORY_WITH_SUBCATEGORIES = "/existsCategoryWithSubCategories";


    //SubCategory
    public static final String SUB_CATEGORY = DEVELOPER+VERSIONS+"/subCategory";
    public static final String VALIDATE_SUB_CATEGORY_EXISTS_BY_ID =
            "/category/{categoryId}/sub-category/validate/{subCategoryId}";

    // Inventory
    public static final String INVENTORY = DEVELOPER+VERSIONS+"/inventory";
    public static final String CREATE_INVENTORY_TO_PRODUCT ="/createInventoryToProduct";
    public static final String GET_INVENTORY_BY_PRODUCT_ID = "/getInventoryByProductId";
    public static final String UPDATE_INVENTORY=  "/updateInventory";
    public static final String DELETE_INVENTORY = "/deleteInventory";

}
