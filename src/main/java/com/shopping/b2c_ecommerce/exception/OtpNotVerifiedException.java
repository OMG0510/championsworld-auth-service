package com.shopping.b2c_ecommerce.exception;

public class OtpNotVerifiedException extends RuntimeException {
    public OtpNotVerifiedException() {
        super("OTP verification required");
    }
}

