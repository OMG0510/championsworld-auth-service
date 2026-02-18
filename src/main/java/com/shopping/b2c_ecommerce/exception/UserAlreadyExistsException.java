package com.shopping.b2c_ecommerce.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String field) {
        super("User already exists with this " + field);
    }
}
