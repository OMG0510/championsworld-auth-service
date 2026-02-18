package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class UserIdentity {
    private Long userId;
    private String email;
    private String role;

    public UserIdentity(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}
