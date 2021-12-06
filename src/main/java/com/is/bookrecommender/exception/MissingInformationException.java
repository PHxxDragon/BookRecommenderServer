package com.is.bookrecommender.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class MissingInformationException extends Exception{
    private String missingField;

    public MissingInformationException(String missingField) {
        super("Missing information of field " + missingField);
        this.missingField = missingField;
    }

    public String getMissingField() {
        return missingField;
    }
}
