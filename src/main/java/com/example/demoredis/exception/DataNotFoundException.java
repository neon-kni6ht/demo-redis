package com.example.demoredis.exception;

public class DataNotFoundException extends RuntimeException{
    public DataNotFoundException() {
    }

    public DataNotFoundException(String message) {
        super(message);
    }
}
