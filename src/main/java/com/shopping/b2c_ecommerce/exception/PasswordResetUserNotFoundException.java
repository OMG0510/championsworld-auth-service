package com.shopping.b2c_ecommerce.exception;

public class PasswordResetUserNotFoundException extends RuntimeException {
    public PasswordResetUserNotFoundException() {
        super("User not found");
    }
}

