package com.is.bookrecommender.service.implementation;

import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.exception.UsernameExistedException;
import com.is.bookrecommender.mapper.ApplicationMapper;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.AuthorityRepository;
import com.is.bookrecommender.repository.UserRepository;
import com.is.bookrecommender.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorityRepository authorityRepository;

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

    public UserDto signUpUser(UserDto userDto) throws UsernameExistedException {
        User existed = userRepository.findUserByUsername(userDto.getUsername());
        if (existed != null) {
            throw new UsernameExistedException(userDto.getUsername());
        }
        User user = mapper.mapUserDtoToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAuthorities(List.of(authorityRepository.findById(1l).orElseThrow()));
        user.setEnabled(true);
        userRepository.save(user);

        return mapper.mapUserToUserDto(user);
    }

    @Override
    public UserDto updateUserProfile(UserDto userDto, String username) {
        User existed = userRepository.findUserByUsername(username);
        if (existed == null) {
            throw new UsernameNotFoundException("Username not found with username: " + userDto.getUsername());
        }
        if (!isStringEmpty(userDto.getName())) {
            existed.setName(userDto.getName());
        }
        if (!isStringEmpty(userDto.getCountry())){
            existed.setCountry(userDto.getCountry());
        }
        if (!isStringEmpty(userDto.getMail())) {
            existed.setMail(userDto.getMail());
        }
        if (userDto.getAge() != null) {
            existed.setAge(userDto.getAge());
        }

        return mapper.mapUserToUserDto(existed);
    }

    private boolean isStringEmpty(String s) {
        return s == null || s.isEmpty() || s.isBlank();
    }


}
