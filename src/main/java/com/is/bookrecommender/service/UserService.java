package com.is.bookrecommender.service;

import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.exception.UsernameExistedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {
    UserDto loadUserProfileByUsername(String s) throws UsernameNotFoundException;

    UserDto signUpUser(UserDto userDto) throws UsernameExistedException;

    UserDto updateUserProfile(UserDto userDto, String username);
}
