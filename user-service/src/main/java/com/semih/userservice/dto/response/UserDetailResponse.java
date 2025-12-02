package com.semih.userservice.dto.response;

import com.semih.userservice.entity.Role;

import java.util.Set;

public record UserDetailResponse(String userName, Set<Role> roleSet) {
}
