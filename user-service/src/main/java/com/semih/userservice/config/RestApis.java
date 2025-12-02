package com.semih.userservice.config;

public final class RestApis {

    private RestApis() {
    }

    public static final String DEVELOPER = "/dev";
    public static final String TEST = "/test";
    public static final String VERSIONS = "/v1";
    public static final String USER = DEVELOPER + VERSIONS + "/user";
    public static final String LOGIN = "/loginUser";
    public static final String REGISTER = "/createUser";
    public static final String REFRESH_TOKEN = "/refreshToken";
    public static final String RESET_PASSWORD = "/resetPassword";
    public static final String UPDATE_PERMISSIONS = "/updatePermissions";
    public static final String DELETE_PERMISSIONS = "/deletePermissions";
    public static final String DELETE_USER = "/deleteUserById/{id}";


}
