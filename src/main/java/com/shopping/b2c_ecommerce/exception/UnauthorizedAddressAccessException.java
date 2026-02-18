package com.shopping.b2c_ecommerce.exception;

public class UnauthorizedAddressAccessException extends RuntimeException {
    public UnauthorizedAddressAccessException() {
        super("You are not allowed to access this address");
    }
}
