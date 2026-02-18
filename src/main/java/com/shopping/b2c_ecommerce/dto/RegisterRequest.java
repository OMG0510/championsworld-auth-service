package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
}