package com.urlshortener.exception;

public class CodeAlreadyExistsException extends RuntimeException {
    public CodeAlreadyExistsException(String code) {
        super("Short code already exists: " + code);
    }
}
