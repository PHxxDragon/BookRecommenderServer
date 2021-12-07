package com.is.bookrecommender.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CannotRetrieveWebResponseException extends Exception{
    private String response;

    public CannotRetrieveWebResponseException(String response) {
        super("Response: " + response);
        this.response = response;
    }
}
