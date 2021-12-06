package com.is.bookrecommender.controller;

import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.exception.UsernameExistedException;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/user/profile")
    public ResponseEntity<?> getUserInfo(Principal user) {
        UserDto userDto = userService.loadUserProfileByUsername(user.getName());
        return ResponseEntity.ok(userDto);
    }

    @PostMapping(value = "/user/signup")
    public ResponseEntity<?> signUpUser(UserDto user) throws UsernameExistedException {
        UserDto result = userService.signUpUser(user);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/user/profile/update")
    public ResponseEntity<?> updateUserProfile(UserDto userDto, Principal user) {
        UserDto result = userService.updateUserProfile(userDto, user.getName());
        return ResponseEntity.ok(result);
    }

}
