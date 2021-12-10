package com.is.bookrecommender.service;

import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.exception.UsernameExistedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService extends UserDetailsService {
    UserDto loadUserProfileByUsername(String s) throws UsernameNotFoundException;

    UserDto signUpUser(UserDto userDto) throws UsernameExistedException;

    UserDto updateUserProfile(UserDto userDto, String username);

    UserDto updateUserAvatar(MultipartFile image, String name) throws IOException;

    static boolean isAdmin() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return false;
        }

        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return false;
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if ("ADMIN".equals(auth.getAuthority()))
                return true;
        }

        return false;
    }
}
