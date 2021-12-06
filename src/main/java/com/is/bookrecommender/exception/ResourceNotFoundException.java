package com.is.bookrecommender.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends Exception{
    private Long resourceId;
    private String message;

    public ResourceNotFoundException(Long resourceId, String errorMessage) {
        super(errorMessage + " ResourceId: " + resourceId);
        this.resourceId = resourceId;
        this.message = errorMessage;
    }

    public Long getResourceId() {
        return resourceId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
