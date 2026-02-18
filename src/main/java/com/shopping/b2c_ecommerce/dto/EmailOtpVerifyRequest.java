package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class EmailOtpVerifyRequest {
    private String email;
    private String otp;
}

