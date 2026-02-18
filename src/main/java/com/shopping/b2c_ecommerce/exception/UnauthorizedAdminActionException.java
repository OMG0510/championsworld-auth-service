package com.shopping.b2c_ecommerce.exception;

public class UnauthorizedAdminActionException extends RuntimeException {
    public UnauthorizedAdminActionException(String message) {
        super(message);
    }
}

