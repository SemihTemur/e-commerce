package com.semih.userservice.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER("User"),
    ROLE_SELLER("Seller"),
    ROLE_ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getAuthority() {
        return this.name();
    }
}
