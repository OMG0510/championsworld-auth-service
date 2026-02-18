package com.shopping.b2c_ecommerce.exception;

public class EmailOtpSendException extends RuntimeException {
    public EmailOtpSendException(String email) {
        super("Failed to send OTP to email: " + email);
    }
}
