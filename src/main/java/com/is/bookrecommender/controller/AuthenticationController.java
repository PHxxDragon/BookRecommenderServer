package com.is.bookrecommender.controller;

import com.is.bookrecommender.config.JWTTokenHelper;
import com.is.bookrecommender.dto.AuthenticationRequest;
import com.is.bookrecommender.dto.AuthenticationResponse;
import com.is.bookrecommender.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JWTTokenHelper jwtTokenHelper;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        String jwtToken = jwtTokenHelper.generateToken(user.getUsername());

        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken(jwtToken);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/userinfo")
    public ResponseEntity<?> getUserInfo(Principal user) {
        User userObj = (User) userDetailsService.loadUserByUsername(user.getName());

        return ResponseEntity.ok(userObj);
    }

}
