package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String role;

    public TokenValidationResponse(boolean valid, Long userId, String role) {
        this.valid = valid;
        this.userId = userId;
        this.role = role;
    }

}
