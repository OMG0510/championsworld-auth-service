package com.shopping.b2c_ecommerce.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException() {
        super("Role not found for user");
    }

    public RoleNotFoundException(String roleName) {
        super("Role not found: " + roleName);
    }
}

