package com.semih.categoryservice.config;

public class RestApis {
    //Category
    public static final String DEVELOPER = "/dev";
    public static final String TEST = "/test";
    public static final String VERSIONS = "/v1";
    public static final String CATEGORY = DEVELOPER+VERSIONS+"/category";
    public static final String CREATE_CATEGORY = "/createCategory";
    public static final String VALIDATE_CATEGORY_HIERARCHY = "/validateCategoryHierarchy";
    public static final String EXISTS_CATEGORY_WITH_SUBCATEGORIES = "/existsCategoryWithSubCategories";
    public static final String GET_CATEGORY_LIST = "/categoryList";
    public static final String GET_CATEGORY_WITH_SUBCATEGORIES_BY_ID = "/getCategoryWıthSubCategoriesById/{categoryId}";
    public static final String GET_CATEGORY_WITH_SUBCATEGORIES_FOR_PRODUCT = "/getCategoryWıthSubCategoriesById/{categoryId}/{subCategoryId}";
    public static final String GET_ALL_CATEGORY_WITH_SUBCATEGORIES = "/getAllCategoryWıthSubCategories";
    public static final String UPDATE_CATEGORY = "/updateCategory/{categoryId}";
    public static final String DELETE_CATEGORY = "/deleteCategory/{categoryId}";

    //SubCategory
    public static final String SUB_CATEGORY = DEVELOPER+VERSIONS+"/subCategory";
    public static final String CREATE_SUBCATEGORY = "/createSubCategory";
    public static final String GET_SUBCATEGORY_LIST = "/subCategoryList";
    public static final String GET_SUBCATEGORY_BY_ID = "/getSubCategoryById";
    public static final String UPDATE_SUBCATEGORY = "/updateSubCategory";
    public static final String DELETE_SUBCATEGORY = "/deleteSubCategory";

}
