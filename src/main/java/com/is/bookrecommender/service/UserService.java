package com.is.bookrecommender.service;

import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.mapper.ApplicationMapper;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(s);
        if (user == null) {
            throw new UsernameNotFoundException("Username not found with username: " + s);
        }
        return user;
    }

    public UserDto loadUserProfileByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(s);
        if (user == null) {
            throw new UsernameNotFoundException("Username not found with username: " + s);
        }
        return mapper.mapUserToUserDto(user);
    }
}
