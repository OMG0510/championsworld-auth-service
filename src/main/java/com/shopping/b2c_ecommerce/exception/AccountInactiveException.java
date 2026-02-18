package com.shopping.b2c_ecommerce.exception;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException() {
        super("Account is inactive");
    }
}

