package com.shopping.b2c_ecommerce.exception;

public class UserNotAdminException extends RuntimeException {
    public UserNotAdminException() {
        super("User is not an admin");
    }
}

