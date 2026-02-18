package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class OtpSendRequest {
    private String email;
    private String mobile;
}

