package com.shopping.b2c_ecommerce.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(Long addressId) {
        super("Address not found with id: " + addressId);
    }
}
