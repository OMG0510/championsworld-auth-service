package com.shopping.b2c_ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAddressResponse {
    private Long addressId;
    private Long userId;
    private String label;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String email;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private Boolean isDefault;
}
