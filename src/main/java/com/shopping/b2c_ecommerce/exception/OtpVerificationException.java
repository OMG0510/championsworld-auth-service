package com.shopping.b2c_ecommerce.exception;

public class OtpVerificationException extends RuntimeException {
    public OtpVerificationException() {
        super("Invalid or expired OTP");
    }
}

