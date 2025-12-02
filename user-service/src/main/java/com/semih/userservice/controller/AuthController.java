package com.semih.userservice.controller;

import static com.semih.userservice.config.RestApis.*;

import com.semih.userservice.dto.request.*;
import com.semih.userservice.dto.response.LoginResponse;
import com.semih.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(USER)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(REGISTER)
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping(LOGIN)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping(REFRESH_TOKEN)
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping(RESET_PASSWORD)
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest){
        return ResponseEntity.ok(authService.resetPassword(changePasswordRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(UPDATE_PERMISSIONS)
    public ResponseEntity<String> updatePermissionsById(@Valid @RequestBody UpdateUserRolesRequest updateUserRolesRequest){
        return ResponseEntity.ok(authService.updatePermissionsById(updateUserRolesRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(DELETE_PERMISSIONS)
    public ResponseEntity<String> deletePermissionById(@Valid @RequestBody RemovePermissionsRequest
                                                                   removePermissionsRequest){
        return ResponseEntity.ok(authService.deletePermissionById(removePermissionsRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(DELETE_USER)
    public ResponseEntity<String> deleteUserById(@PathVariable Long id){
        return ResponseEntity.ok(authService.deleteUserById(id));
    }

}
