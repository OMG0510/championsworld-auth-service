package com.shopping.b2c_ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatusResponse {
    private Long adminId;
    private boolean active;
    private String message;
}
