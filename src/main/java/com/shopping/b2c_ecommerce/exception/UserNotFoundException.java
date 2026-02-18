package com.shopping.b2c_ecommerce.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId)
    {
        super("User not found with id: " + userId);
    }
    public UserNotFoundException(String email)
    {
        super("User not registered with email / phone: " + email);
    }

}
