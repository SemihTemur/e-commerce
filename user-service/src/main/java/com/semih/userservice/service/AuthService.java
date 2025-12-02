package com.semih.userservice.service;

import com.semih.userservice.dto.request.*;
import com.semih.userservice.dto.response.LoginResponse;
import com.semih.userservice.entity.Role;
import com.semih.userservice.entity.User;
import com.semih.userservice.exception.*;
import com.semih.userservice.repository.UserRepository;
import com.semih.userservice.util.RedisUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService{

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenService jwtTokenService;
    private final RedisUtil redisUtil;

    public AuthService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository,
                       AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService,
                       JwtTokenService jwtTokenService, RedisUtil redisUtil) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenService = jwtTokenService;
        this.redisUtil = redisUtil;
    }

    public String register(RegisterRequest registerRequest){
        User user = mapToUser(registerRequest);

        userRepository.save(user);

        Set<String> roles = registerRequest.roles()
                .stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet());

        redisUtil.saveUserPermissionsToCache("permission",user.getId().toString(),roles);

        return "Successfully";
    }

    public LoginResponse login(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password()));

        User user = (User) authentication.getPrincipal();

        if(user==null)
            throw new UserNotFoundException("Kullanııcı bulunamadı!");

        return generateTokensForAuthenticatedUser(user);
    }

    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest){
        User user = refreshTokenService.rotateRefreshToken(refreshTokenRequest.refreshToken())
                .orElseThrow(()-> new UserNotFoundException("Kullanıcı bulunamadı!"));

        return generateTokensForAuthenticatedUser(user);
    }

    public String resetPassword(ChangePasswordRequest changePasswordRequest){
        User user = getUserOrThrow(authenticatedUserId());

        if(!bCryptPasswordEncoder.matches(changePasswordRequest.currentPassword(),user.getPassword()))
           throw new InvalidPasswordException("Şifre hatalı. Lütfen tekrar deneyiniz!");

        if(bCryptPasswordEncoder.matches(changePasswordRequest.newPassword(),user.getPassword()))
            throw new NewPasswordSameAsOldException("Yeni şifre mevcut şifre ile aynı olamaz!");

        user.setPassword(bCryptPasswordEncoder.encode(changePasswordRequest.newPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }

    @Transactional
    public String updatePermissionsById(UpdateUserRolesRequest updateUserRolesRequest){
        User user = getUserOrThrow(updateUserRolesRequest.userId());
        Set<Role> roles = user.getRole();

        for(Role role:updateUserRolesRequest.roles()){
            if(roles.contains(role))
                throw  new AuthorityAlreadyExistsException("Bu yetki sistemde zaten kayıtlı!");

            roles.add(role);
        }

        userRepository.save(user);

        Set<String> roleSet = user.getRole()
                .stream().map(Role::getAuthority)
                .collect(Collectors.toSet());

        redisUtil.saveUserPermissionsToCache("permission",user.getId().toString(),roleSet);

        return "Successfully";
    }

    @Transactional
    public String deletePermissionById(RemovePermissionsRequest removePermissionsRequest){
        User user = getUserOrThrow(removePermissionsRequest.userId());
        Set<Role> roles = user.getRole();

        for(Role role:removePermissionsRequest.roles()){
            if(!roles.contains(role))
                throw new AuthorityNotFoundException("Kullanıcıda belirtilen yetki mevcut değil!");

            roles.remove(role);
        }

        userRepository.save(user);

        Set<String> roleSet = user.getRole()
                .stream().map(Role::getAuthority)
                        .collect(Collectors.toSet());

        redisUtil.saveUserPermissionsToCache("permission",user.getId().toString(),roleSet);

        return "Successfully";
    }

    @Transactional
    public String deleteUserById(Long id){
        User user = getUserOrThrow(id);
        userRepository.delete(user);

        redisUtil.deleteUserPermissionsToCache("permission",user.getId().toString());

        return "Successfully";
    }

    private User mapToUser(RegisterRequest registerRequest){
        return new User(registerRequest.userName(),registerRequest.email(),
                bCryptPasswordEncoder.encode(registerRequest.password()), registerRequest.roles());
    }

    private LoginResponse generateTokensForAuthenticatedUser(User user){
            String accessToken = jwtTokenService.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshTokenForUser(user);

            return new LoginResponse(accessToken,refreshToken);
    }

    private User getUserOrThrow(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("Kullanııcı bulunamadı!"));
    }

    private Long authenticatedUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UserNotVerifiedException("Kullanıcı henüz doğrulanmamış!");
        }

        String idStr = (String) authentication.getPrincipal();
        return  Long.valueOf(idStr);
    }
}
