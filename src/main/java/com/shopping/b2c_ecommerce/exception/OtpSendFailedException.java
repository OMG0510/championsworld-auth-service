package com.shopping.b2c_ecommerce.exception;

public class OtpSendFailedException extends RuntimeException {
    public OtpSendFailedException() {
        super("Failed to send OTP");
    }
}