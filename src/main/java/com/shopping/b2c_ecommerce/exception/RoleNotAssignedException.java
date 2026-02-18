package com.shopping.b2c_ecommerce.exception;

public class RoleNotAssignedException extends RuntimeException {
    public RoleNotAssignedException() {
        super("Role not assigned to user");
    }
}

