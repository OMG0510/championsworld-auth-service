package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class AdminSummaryResponse {
    private Long id;
    private String email;
    private Boolean active;

    public AdminSummaryResponse(Long id, String email, Boolean active) {
        this.id = id;
        this.email = email;
        this.active = active;
    }
}
