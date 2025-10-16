package com.github.jakubpakula1.lab.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
