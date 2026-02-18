package com.shopping.b2c_ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressResponse {
    private Long addressId;
    private boolean isDefault;
    private String message;
}