package com.is.bookrecommender.controller;

import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
