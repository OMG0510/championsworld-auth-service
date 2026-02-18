package com.shopping.b2c_ecommerce.util;

import com.shopping.b2c_ecommerce.dto.UserAddressResponse;
import com.shopping.b2c_ecommerce.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class Utilities {
    //Utility Method for UserAddressResponse
    public UserAddressResponse mapToResponse(Address address) {
        return new UserAddressResponse(
                address.getId(),
                address.getUser().getId(),
                address.getLabel(),
                address.getFirstName(),
                address.getLastName(),
                address.getContactNumber(),
                address.getEmail(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry(),
                address.getIsDefault()
        );
    }
}
