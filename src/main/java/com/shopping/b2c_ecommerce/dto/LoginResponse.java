package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private Long userId;
    private String email;
    private String token;
    private String tokenType = "Bearer";

    public LoginResponse(Long userId,String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

}
