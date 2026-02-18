package com.shopping.b2c_ecommerce.exception;

public class GoogleUserInfoException extends RuntimeException {
    public GoogleUserInfoException() {
        super("Failed to fetch Google user information");
    }
}