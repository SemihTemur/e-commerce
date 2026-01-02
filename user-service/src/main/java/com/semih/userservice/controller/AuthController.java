package com.semih.userservice.controller;

import com.semih.userservice.dto.request.*;
import com.semih.userservice.dto.response.LoginResponse;
import com.semih.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.semih.common.config.RestApis.*;

@RestController
@RequestMapping(USERS)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // User-specific actions
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ChangePasswordRequest request){
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // Admin-specific actions
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/permissions")
    public ResponseEntity<String> updatePermissions(@Valid @RequestBody UpdateUserRolesRequest request){
        return ResponseEntity.ok(authService.updatePermissionsById(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/permissions")
    public ResponseEntity<String> deletePermissions(@Valid @RequestBody RemovePermissionsRequest request){
        return ResponseEntity.ok(authService.deletePermissionById(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        return ResponseEntity.ok(authService.deleteUserById(id));
    }
}
