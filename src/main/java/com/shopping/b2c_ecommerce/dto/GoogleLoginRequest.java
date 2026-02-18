package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String email;
    private String googleId;
}
