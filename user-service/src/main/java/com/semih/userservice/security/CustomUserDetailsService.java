package com.semih.userservice.security;

import com.semih.userservice.exception.UserNotFoundException;
import com.semih.userservice.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username){
        return userRepository.findByEmail(username)
                .orElseThrow(()-> new UserNotFoundException("Kullanıcı bulunamadı"));
    }
}
