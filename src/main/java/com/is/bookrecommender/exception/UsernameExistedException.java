package com.is.bookrecommender.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UsernameExistedException extends Exception{
    private String username;

    public UsernameExistedException(String username) {
        super("Username existed: " + username);
        this.username = username;
    }
}
