package com.shopping.b2c_ecommerce.exception;

public class PasswordResetTokenNotFoundException extends RuntimeException {
    public PasswordResetTokenNotFoundException() {
        super("OTP not found");
    }
}

